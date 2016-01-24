package resourceplanner.controllers;

import databases.JDBC;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import requestdata.ResourceRequest;
import responses.StandardResponse;
import responses.data.Resource;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Controller {
	public boolean isAdmin(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
        int userId = Integer.parseInt(claims.get("user_id").toString());
        if (userId == 1) {
            return true;
        }
        else{
        	return false;
        }

	}

	public int getRequesterID(HttpServletRequest request){
		final Claims claims = (Claims) request.getAttribute("claims");
    	//Verify that the user_id in the requesrt == current user
    	int requesterID = Integer.parseInt(claims.get("user_id").toString());
    	return requesterID;
	}
}