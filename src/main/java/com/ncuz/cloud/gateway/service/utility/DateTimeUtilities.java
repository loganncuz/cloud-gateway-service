package com.ncuz.cloud.gateway.service.utility;

import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
public class DateTimeUtilities {
    public String getTimeZoneFormat(String readFormat,String readDate,String writeFormat,String timeZone){
        String datestr=readDate;
        DateFormat readF = new SimpleDateFormat( readFormat);
        DateFormat writeF = new SimpleDateFormat( writeFormat);
        Date date1 = null;
        String formattedDate = "";
        if(readDate!=null){
            try {
                date1=readF.parse(datestr);
//            logger.debug("readDate :"+date1);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            if( date1 != null ) {
                if(timeZone!=null) {
                    writeF.setTimeZone(TimeZone.getTimeZone(timeZone));
                }
                formattedDate = writeF.format( date1 );
//            logger.debug("formattedDate getGrafanaDateFormat :"+formattedDate);
            }
        }

//        logger.debug("ts :"+ts);
//        logger.debug("Date format dd-mm-yy :"+ts+" | "+epoc);
        try{
            return formattedDate;
        }finally {
            date1=null;
            formattedDate=null;
            writeF=null;
            readF=null;
            datestr=null;
//            System.gc();
        }
    }
}
