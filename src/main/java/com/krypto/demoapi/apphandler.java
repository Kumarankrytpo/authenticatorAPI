package com.krypto.demoapi;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.krypto.Mail.mailSend;
import com.krypto.connection.ServerConnection;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("auth")
public class apphandler {

    /**
     * Method handling HTTP GET requests. The returned object will be sent to
     * the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    ServerConnection server = new ServerConnection();
    mailSend mail = new mailSend();

    @POST
    @Path("/login")
    public Response login(String data) throws SQLException {
        String jsondata = "";
        HashMap map = new HashMap();
        map = server.loginCheck(data);
        boolean status = (boolean) map.get("status");
        if (status) {
            map.remove("status");
            String userid = String.valueOf(map.get("userid"));
            String username = String.valueOf(map.get("username"));
            String accessToken = TokenManager.createAccessToken(userid, username);
            String refreshToken = TokenManager.createRefreshToken(userid);
            map.put("check", "success");
            map.put("accesstoken", accessToken);
            map.put("refreshtoken", refreshToken);
            map.put("username", username);
        } else {
            map.put("check", "user Not Exists");
        }
        Gson gs = new Gson();
        jsondata = gs.toJson(map);
        System.out.println("THIS IS JSON DATA >>>" + jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/usercreation")
    public Response signup(String request) throws SQLException {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(request);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    HashMap map = new HashMap();
                    if (server.newUserCheck(request)) {
                        boolean rtn = server.newUserSave(request);
                        if (rtn) {
                            map.put("status", "success");
                        } else {
                            map.put("status", "unsuccessful");
                        }
                    } else {
                        map.put("status", "existing user");
                    }
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                }

            } else {
                HashMap map = new HashMap();
                map.put("access", "accessexpired");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/authCodeIntiation")
    public Response authCodeIntiation(String data) {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    HashMap map = server.authcodesave(data);
                    map.put("status", "success");
                    map.put("emailid", js.getString("emailid"));
                    HashMap mailMap = mail.mailsend(map);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(mailMap);
                }
            } else {
                HashMap map = new HashMap();
                map.put("access", "accessexpired");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/getauthcode")
    public Response getauthcode(String data) {
        String jsondata = "";
        try {
            HashMap map = server.getauth(data);
            Gson gs = new Gson();
            jsondata = gs.toJson(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/authCodeCheck")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authCodeCheck(String data) {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                System.out.println("STATUS >>>" + status);
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("THIS IS AUTH CODE CHECK");
                    HashMap map = server.authCodeCheck(data);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/authCodeUpdate")
    public Response authCodeUpdate(String data) {
        String jsondata = "";
        try {
            HashMap map = new HashMap();
            if (server.authCodeAvailable(data)) {
                map = server.authcodesave(data);
            } else {
                map.put("status", "loginSuccessful");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/getEmpID")
    public Response getEMPID(String data) {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    HashMap map = server.getempcode();
                    map.put("status", "success");
                    map.put("userlist", jsondata);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    public HashMap accessVerify(String accesstoken, String refreshToken, String username) {
        HashMap rtnmap = new HashMap();
        try {
            DecodedJWT decodedAccessToken = TokenManager.verifyToken(accesstoken);
            System.out.println("Access Token is valid. Username: " + decodedAccessToken.getClaim("username").asString());
            rtnmap.put("status", "success");
        } catch (JWTVerificationException e) {
            System.err.println("Access Token is invalid or expired.");
            rtnmap = refreshVerify(refreshToken, username);
        }
        return rtnmap;
    }

    public HashMap refreshVerify(String refreshToken, String username) {
        HashMap rtnmap = new HashMap();
        try {
            DecodedJWT decodedRefreshToken = TokenManager.verifyToken(refreshToken);
            String newAccessToken = TokenManager.createAccessToken(decodedRefreshToken.getSubject(), username);
            System.out.println("New Access Token::: " + newAccessToken);
            rtnmap.put("accesstoken", newAccessToken);
            rtnmap.put("status", "tokenrefreshed");
        } catch (JWTVerificationException e) {
            System.err.println("Refresh Token is invalid or expired. User needs to re-authenticate.");
            rtnmap.put("status", "sessionexpired");
        }
        return rtnmap;
    }

    @POST
    @Path("/getUsers")
    public Response getUsers(String data) {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    JSONArray list = server.getUsers(data);
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    map.put("userlist", list);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/getTeamMember")
    public Response getTeamMember(String data) {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    JSONArray list = server.getusers(data);
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    map.put("userlist", list);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/saveTaskDetails")
    public Response saveTaskDetails(String data) {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    HashMap taskmap = server.savetask(data);
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/getRoleDetails")
    public Response getRoleDetails(String data) {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    JSONArray rolearr = server.getroles();
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    map.put("roledetails", rolearr);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/saveRoles")
    public Response saveRoles(String data) {
        String jsondata = "";
        try {
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    server.saveroles(data);
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/getTaskDetails")
    public Response getTaskDetails(String data) {
        String jsondata = "";
        try {
            System.out.println("THIS IS TASK STRING DATA " + data);
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                System.out.println("THIS IS TOKEN STATUS >>" + status);
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    JSONArray taskarr = server.gettasks(js.getString(("empcode")));
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    map.put("taskdetails", taskarr);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THIS IS JSON DATA " + jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/getPendingTaskDetails")
    public Response getPendingTaskDetails(String data) {
        String jsondata = "";
        try {
            System.out.println("THIS IS TASK STRING DATA " + data);
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                System.out.println("THIS IS TOKEN STATUS >>" + status);
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    JSONArray taskarr = server.getpendingtasks(js.getString(("empcode")));
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    map.put("pendingtaskdetails", taskarr);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THIS IS JSON DATA " + jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/getTaskNumbers")
    public Response getTaskNumbers(String data) {
        String jsondata = "";
        try {
            System.out.println("THIS IS TASK STRING DATA " + data);
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                System.out.println("THIS IS TOKEN STATUS >>" + status);
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    JSONArray taskarr = server.gettasknumbers(js.getString(("empcode")));
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    map.put("ongoingtaskno", taskarr.get(0));
                    map.put("overduetaskno", taskarr.get(1));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THIS IS JSON DATA " + jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/setCompleteTask")
    public Response completeTask(String data) {
        String jsondata = "";
        try {
            System.out.println("THIS IS TASK STRING DATA " + data);
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                System.out.println("THIS IS TOKEN STATUS >>" + status);
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    boolean flag = server.setCompleteTask(js);
                    HashMap map = new HashMap();
                    if (flag) {
                        map.put("status", "success");
                    } else {
                        map.put("status", "Error");
                    }

                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THIS IS JSON DATA " + jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/getCompleteTask")
    public Response getCompleteTask(String data) {
        String jsondata = "";
        try {
            System.out.println("THIS IS TASK STRING DATA " + data);
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                System.out.println("THIS IS TOKEN STATUS >>" + status);
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    JSONArray taskarr = server.getCompletedTask(js);
                    HashMap map = new HashMap();
                    map.put("status", "success");
                    map.put("taskdata", taskarr);
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THIS IS JSON DATA " + jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

    @POST
    @Path("/setExtendTask")
    public Response setExtendTask(String data) {
        String jsondata = "";
        try {
            System.out.println("THIS IS TASK STRING DATA " + data);
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                System.out.println("THIS IS TOKEN STATUS >>" + status);
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    boolean flag = server.saveExtendTaskDetail(js);
                    HashMap map = new HashMap();
                    if (flag) {
                        map.put("status", "success");
                    } else {
                        map.put("status", "Error");
                    }

                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THIS IS JSON DATA " + jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }
    
    @POST
    @Path("/getBarChartDetails")
    public Response getBarChartDetails(String data) {
        String jsondata = "";
        try {
            System.out.println("THIS IS TASK STRING DATA " + data);
            JSONObject js = new JSONObject(data);
            String accesstoken = js.getString("Accesstoken");
            String refreshToken = js.getString("RefreshToken");
            String username = js.getString("username");
            if (accesstoken != null && accesstoken.length() > 0 && refreshToken != null && refreshToken.length() > 0) {
                HashMap accessMap = accessVerify(accesstoken, refreshToken, username);
                String status = (String) accessMap.get("status");
                System.out.println("THIS IS TOKEN STATUS >>" + status);
                if (status.equalsIgnoreCase("sessionexpired")) {
                    HashMap map = new HashMap();
                    map.put("status", "sessionexpired");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("tokenrefreshed")) {
                    HashMap map = new HashMap();
                    map.put("status", "tokenrefreshed");
                    map.put("token", accessMap.get("accesstoken"));
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                } else if (status.equalsIgnoreCase("success")) {
                    System.out.println("INSIDE GET MANAEGRS LIST ");
                    HashMap map = server.getBarChartDetails(js.getString("empcode"));
                    map.put("status", "success");
                    Gson gs = new Gson();
                    jsondata = gs.toJson(map);
                    System.out.println("THIS IS JSON DATA >>" + jsondata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("THIS IS JSON DATA " + jsondata);
        ResponseBuilder response = Response.ok();
        response.entity(jsondata);
        response.header("Access-Control-Allow-Origin", "*");
        return response.build();
    }

}
