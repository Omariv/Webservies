package com.WS_Route;

public class CreateRoute {

	public static void main(String[] args) {
		try {
			RouteManager cam = new RouteManager(Utils.USER, Utils.CONTEXT, Utils.inputfile);
			cam.CreateRoute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
