package org.eclipse.jetty.util.log;

import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.log.Slf4jLog;

public class ConstellioSlf4jJettyLog extends AbstractLogger
{
    private final org.slf4j.Logger _logger;

    public ConstellioSlf4jJettyLog() throws Exception
    {
        this("org.eclipse.jetty.util.log");
    }

    public ConstellioSlf4jJettyLog(String name)
    {
        //NOTE: if only an slf4j-api jar is on the classpath, slf4j will use a NOPLogger
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger( name );
        
        // Fix LocationAwareLogger use to indicate FQCN of this class - 
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=276670
        if (logger instanceof org.slf4j.spi.LocationAwareLogger)
        {
            _logger = new ConstellioJettyAwareLogger((org.slf4j.spi.LocationAwareLogger)logger);
        }
        else
        {
            _logger = logger;
        }
    }

    public String getName()
    {
        return _logger.getName();
    }

    public void warn(String msg, Object... args)
    {
        _logger.warn(msg, args);
    }

    public void warn(Throwable thrown)
    {
        warn("", thrown);
    }

    public void warn(String msg, Throwable thrown)
    {
        _logger.warn(msg, thrown);
    }

    public void info(String msg, Object... args)
    {
        _logger.info(msg, args);
    }

    public void info(Throwable thrown)
    {
        info("", thrown);
    }

    public void info(String msg, Throwable thrown)
    {
        _logger.info(msg, thrown);
    }

    public void debug(String msg, Object... args)
    {
        _logger.debug(msg, args);
    }

    public void debug(Throwable thrown)
    {
        debug("", thrown);
    }

    public void debug(String msg, Throwable thrown)
    {
        _logger.debug(msg, thrown);
    }

    public boolean isDebugEnabled()
    {
        return _logger.isDebugEnabled();
    }

    public void setDebugEnabled(boolean enabled)
    {
        warn("setDebugEnabled not implemented",null,null);
    }

    /**
     * Create a Child Logger of this Logger.
     */
    protected Logger newLogger(String fullname)
    {
        return new Slf4jLog(fullname);
    }

    public void ignore(Throwable ignored)
    {
        if (Log.isIgnored())
        {
            warn(Log.IGNORED, ignored);
        }
    }

    @Override
    public String toString()
    {
        return _logger.toString();
    }

}
