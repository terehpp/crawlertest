package org.terehpp.crawler.component.analyzer;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateTime adapter, because of XSD schema does not support pattern of date.
 */
public class XMLDateTimeAdapter extends XmlAdapter<String, Date> {
    /**
     * Unmarshal string to date.
     *
     * @param v String value.
     * @return Date.
     * @throws Exception
     */
    @Override
    public Date unmarshal(String v) throws Exception {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        try {
            return formatter.parse(v);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Marshal Date.
     *
     * @param v Value.
     * @return String of date.
     * @throws Exception
     */
    @Override
    public String marshal(Date v) throws Exception {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        return formatter.format(v);
    }
}
