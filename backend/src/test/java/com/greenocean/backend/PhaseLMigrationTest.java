package com.greenocean.backend;
import org.flywaydb.core.Flyway;import org.junit.jupiter.api.Test;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.jdbc.datasource.DriverManagerDataSource;import java.util.*;import static org.assertj.core.api.Assertions.*;
class PhaseLMigrationTest extends MigrationTestSupport {
 @Test void populatedKRetainsIdentityAudienceAndRelations(){
  String url=System.getenv("GREENOCEAN_TEST_DB_URL"),schema="qa_l_"+UUID.randomUUID().toString().replace("-","");var source=new DriverManagerDataSource(url,System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD",""));
  Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).target("43").load().migrate();var db=new JdbcTemplate(new DriverManagerDataSource(url+(url.contains("?")?"&":"?")+"currentSchema="+schema+",public",System.getenv().getOrDefault("GREENOCEAN_TEST_DB_USER","postgres"),System.getenv().getOrDefault("GREENOCEAN_TEST_DB_PASSWORD","")));
  try{UUID owner=db.queryForObject("INSERT INTO users(email,password_hash,status) VALUES('l-legacy@example.test','preserved-hash','ACTIVE') RETURNING id",UUID.class);db.update("INSERT INTO profiles(user_id,username,display_name,is_profile_private) VALUES(?,'l_legacy','Historical',TRUE)",owner);
   for(String status:List.of("PUBLISHED","HIDDEN","UNDER_REVIEW","DELETED"))for(boolean anonymous:List.of(true,false)){
    UUID id=db.queryForObject("INSERT INTO posts(author_id,body,is_anonymous,visibility,status) VALUES(?,'Historical content',?,'FOLLOWERS',?) RETURNING id",UUID.class,owner,anonymous,status);
    db.update("INSERT INTO comments(post_id,author_id,body,is_anonymous) VALUES(?,?,'Historical reply',?)",id,owner,anonymous);db.update("INSERT INTO bookmarks(user_id,post_id) VALUES(?,?)",owner,id);
   }
   var before=db.queryForList("SELECT id,author_id,is_anonymous,visibility,status FROM posts ORDER BY id");
   var latest=Flyway.configure().dataSource(source).schemas(schema).defaultSchema(schema).load();latest.migrate();latest.validate();
   assertThat(db.queryForList("SELECT id,author_id,is_anonymous,visibility,status FROM posts ORDER BY id")).isEqualTo(before);
   assertThat(db.queryForObject("SELECT COUNT(*) FROM bookmarks WHERE user_id=?",Integer.class,owner)).isEqualTo(8);
   assertThat(db.queryForObject("SELECT COUNT(*) FROM social_assessments",Integer.class)).isZero();
   assertThat(db.queryForObject("SELECT COUNT(*) FROM social_drafts",Integer.class)).isZero();
   assertThat(db.queryForObject("SELECT is_profile_private FROM profiles WHERE user_id=?",Boolean.class,owner)).isTrue();
  }finally{db.execute("DROP SCHEMA "+schema+" CASCADE");}
 }
}
