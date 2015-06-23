package top.appcity.websnapasimg.log;

import java.io.IOException;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class LogBackExEncoder extends PatternLayoutEncoder {
	static {
		PatternLayout.defaultConverterMap.put("T", ThreadNumConverter.class.getName());
		PatternLayout.defaultConverterMap.put("threadNum", ThreadNumConverter.class.getName());
	}
	@Override
	public void doEncode(ILoggingEvent event) throws IOException {
		super.doEncode(event);
	}
	

}
