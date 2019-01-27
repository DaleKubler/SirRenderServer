package tv.culturesport.sirrender;


import javax.swing.JTextArea;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;


public class JTextAreaAppender extends AppenderSkeleton {

	public JTextArea _jtextarea = null;
	
	private Layout _pl = new FlexibleLayout();
	
	public JTextAreaAppender(){}
	
	public JTextAreaAppender(JTextArea j){
		super();
		_jtextarea = j;
	}
	
	@Override
	protected void append(LoggingEvent event) {   	
	    String toLog = _pl.format(event);
	    if (_jtextarea == null){
	    	_jtextarea = Console.getTextArea();
	    }
    	log(toLog);
	}
	
	private void log(String s) {
		if (s== null) return;
		if (_jtextarea == null) return;
		_jtextarea.append(s);
	}
	
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requiresLayout() {		
		return false;
	}

	
}
