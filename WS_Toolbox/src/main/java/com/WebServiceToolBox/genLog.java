package com.WebServiceToolBox;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;


public class genLog { 
    
    public genLog(String VMs, String step, String status, String type, String name,String revision, int errorCode, String errorDetail,String id,String logFile) throws FileNotFoundException {
		     
	     LocalDate d =  LocalDate.now();
	     LocalTime t =  LocalTime.now(); 
	    // LocalDateTime dt = LocalDateTime.now(); 

	     	     
	     try(FileWriter fw = new FileWriter(logFile, true);
	    		 BufferedWriter bw = new BufferedWriter(fw);
	    		 PrintWriter out = new PrintWriter(fw))
	    		{
	    		 out.println(d+" "+t+";"+VMs+";"+step+";"+status+";"+type+";"+name+";"+revision+";"+errorCode+";"+id+";"+errorDetail);
	    		 //System.out.println(d+" "+t+";"+VMs+";"+step+";"+status+";"+type+";"+name+";"+revision+";"+errorCode+";"+errorDetail);
	    		}
	    		catch (IOException e) {	    		 
	    		}     
	}
}