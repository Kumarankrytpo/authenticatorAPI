package com.krypto.demoapi;

import com.google.gson.Gson;
import com.krypto.connection.ServerConnection;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("auth")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    ServerConnection server = new ServerConnection();
    
    @POST
    @Path("/login")
    public Response login(String data)throws SQLException {
        String jsondata = "";
        HashMap map = new HashMap();
        if(server.loginCheck(data)){
            map.put("status","success");
        }else{
            map.put("status","user Not Exists");
        }
        Gson gs = new Gson();
        jsondata = gs.toJson(map);
        System.out.println("THIS IS JSON DATA >>>"+jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }
    
    @POST
    @Path("/signup")
    public Response signup(String request) throws SQLException{
        System.out.println("INSIDE REQUEST DATA : " + request);
        String jsondata = "";
        HashMap map = new HashMap();
        if (server.newUserCheck(request)) {
            boolean rtn = server.newUserSave(request);
            if(rtn){
                map.put("status","success");
            }else{
                map.put("status","unsuccessful");
            }
        }else{
            map.put("status","existing user");
        }
        Gson gs = new Gson();
        jsondata = gs.toJson(map);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }
}
