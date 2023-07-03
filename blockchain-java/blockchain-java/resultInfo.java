package com.simplilearn.mavenproject;

import java.io.PrintWriter;

public class resultInfo 
{
	public static final int ERROR = 0 ;
	public static final int INFO  = 1 ;
	public static final int DEBUG = 2 ;
	
	public boolean	m_hasError		= false ;
	public String	m_errorString	= new String() ;
	public boolean	m_stopProcess	= false ;
	public int		m_loglevel		= ERROR ;
	
	public resultInfo()
	{
		
	}
	
	public resultInfo(int loglevel)
	{
		m_loglevel = loglevel ;
	}

	public void raiseError(String errStr)
	{
		m_hasError = true ;
		m_errorString = errStr ;
		
		trace(ERROR, errStr) ;
	}
	public void trace(int traceLevel, String msg)
	{
        if (m_loglevel >= traceLevel)
        {            
        	System.out.println(msg);
        }
	}
	public boolean hasErrors()
	{
		return(m_hasError);
	}
	public String errorString()
	{
		return(m_errorString);
	}
}
