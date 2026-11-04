package com.greenocean.backend.auth.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;

/** Limits parser work without retaining, logging or duplicating sensitive JSON bodies. */
@Component @Order(-110)
public class BoundedJsonFilter extends OncePerRequestFilter {
 public static final long MAX=1048576;
 public static class Limit extends RuntimeException {public Limit(){super("JSON request exceeds the bounded input limit");}}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws IOException,ServletException{
  String type=request.getContentType(),mime=type==null?"":type.toLowerCase(java.util.Locale.ROOT).split(";",2)[0].trim();if(!mime.equals("application/json")&&!(mime.startsWith("application/")&&mime.endsWith("+json"))){chain.doFilter(request,response);return;}
  if(request.getContentLengthLong()>MAX){response.setStatus(413);response.setHeader("Cache-Control","no-store");response.setContentType("application/json");response.getWriter().write("{\"message\":\"JSON request exceeds the bounded input limit\"}");return;}
  chain.doFilter(new HttpServletRequestWrapper(request){
   @Override public ServletInputStream getInputStream()throws IOException{var input=super.getInputStream();return new ServletInputStream(){long count;private void added(int n){if(n>0&&(count+=n)>MAX)throw new Limit();}public int read()throws IOException{int v=input.read();if(v>=0)added(1);return v;}public int read(byte[] bytes,int offset,int length)throws IOException{int n=input.read(bytes,offset,(int)Math.min(length,MAX-count+1));added(n);return n;}public boolean isFinished(){return input.isFinished();}public boolean isReady(){return input.isReady();}public void setReadListener(ReadListener listener){input.setReadListener(listener);}};}
   @Override public BufferedReader getReader()throws IOException{return new BufferedReader(new InputStreamReader(getInputStream(),java.nio.charset.StandardCharsets.UTF_8));}
  },response);
 }
}
