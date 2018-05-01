package org.wildfly.swarm.jaxrs;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseLookup;

@RunWith(Arquillian.class)
public class JoseJwsCompactDetachedTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(MyResource.class);
        deployment.addResource(SecuredApplication.class);
        deployment.addResource(JoseExceptionMapper.class); 
        deployment.addAllDependencies();
        deployment.addAsResource("keystore.jks");
        deployment.addAsResource("project-jws-compact-detached.yml", "project-defaults.yml");
        return deployment;
    }
    
    @Test
    public void testJwsCompactDetached() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        Response r = ClientBuilder.newClient().target("http://localhost:8080/signDetached")
                                .request(MediaType.TEXT_PLAIN)
                                .header("DetachedData", "Hello")
                                .post(Entity.entity(jose.sign("Hello"), MediaType.TEXT_PLAIN));
        String jws = r.readEntity(String.class);
        Assert.assertEquals("Hello", jose.verifyDetached(jws, r.getHeaderString("DetachedData")));
    }
       
}
