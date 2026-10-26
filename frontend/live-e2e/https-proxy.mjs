// Test-only same-origin front door: actual backend APIs and Expo UI on HTTP + HTTPS.
import https from 'node:https';import http from 'node:http';import {readFileSync} from 'node:fs';import {spawn} from 'node:child_process';
const expo=spawn(process.execPath,['node_modules/expo/bin/cli','start','--web','--port','8081'],{env:{...process.env,EXPO_PUBLIC_WEB_API_ORIGIN:'same-origin'},stdio:'inherit'});
function proxy(req,res){const port=req.url.startsWith('/api/')?8080:8081;const upstream=http.request({hostname:'127.0.0.1',port,path:req.url,method:req.method,headers:{...req.headers,host:`127.0.0.1:${port}`}},reply=>{res.writeHead(reply.statusCode,reply.headers);reply.pipe(res);});upstream.on('error',()=>{res.writeHead(503);res.end();});req.pipe(upstream);}
const plain=http.createServer(proxy),secure=https.createServer({key:readFileSync('../backend/target/qa-tls.key'),cert:readFileSync('../backend/target/qa-tls.crt')},proxy);plain.listen(8082,'127.0.0.1');secure.listen(8443,'127.0.0.1');
process.on('SIGTERM',()=>{expo.kill();plain.close();secure.close();});process.on('exit',()=>expo.kill());
