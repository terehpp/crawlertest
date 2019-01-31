package org.terehpp.crawler.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terehpp.crawler.constants.AppPropName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Property helper - utility class.
 */
public class PropertyHelper {
    private final static Log logger = LogFactory.getLog(PropertyHelper.class);

    private PropertyHelper() {
    }

    /**
     * Initialize property from outside file.
     *
     * @param args       Input args.
     * @param properties Properties to initialize.
     * @return Is success.
     */
    public static boolean initProperties(String[] args, Properties properties) {
        if (args.length > 1) {
            boolean isPathToProperties = false;
            for (String arg : args) {
                if (isPathToProperties) {
                    try {
                        properties.load(new FileInputStream(arg));
                        return true;
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (AppPropName.PATH_TO_PROPERTIES_ARG.equals(arg)) {
                    isPathToProperties = true;
                }
            }
        }
        logger.error(String.format("No flag %s found", AppPropName.PATH_TO_PROPERTIES_ARG));
        return false;
    }

    /**
     * Get int property.
     *
     * @param properties Properties.
     * @param key        Key of property.
     * @return Value.
     * @throws PropertyHelperException
     */
    public static int getIntProperty(final Properties properties, String key) throws PropertyHelperException {
        String property = properties.getProperty(key);
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException e) {
            logger.error(e.getMessage(), e);
            throw new PropertyHelperException(String.format("Please specify the %s property", key));
        }
    }

    /**
     * Get positive int property.
     *
     * @param properties Properties.
     * @param key        Key of property.
     * @return Value.
     * @throws PropertyHelperException
     */
    public static int getPositiveIntProperty(final Properties properties, String key) throws PropertyHelperException {
        int res = getIntProperty(properties, key);
        if (res <= 0) {
            throw new PropertyHelperException(String.format("%s property should be greater then 0", key));
        }
        return res;
    }

    /**
     * Get string property.
     *
     * @param properties Properties.
     * @param key        Key of property.
     * @return Value.
     * @throws PropertyHelperException
     */
    public static String getStrProperty(final Properties properties, String key) throws PropertyHelperException {
        String property = properties.getProperty(key);
        if (property == null || "".equals(property.trim())) {
            throw new PropertyHelperException(String.format("Please specify the %s property", key));
        }
        return property;
    }

    /**
     * Get path from properties and check if it exist and needed rights exist.
     *
     * @param properties Properties.
     * @param key        Key of property.
     * @param canRead    Check rights on read.
     * @param canWrite   Check rights on write.
     * @return Directory path.
     * @throws PropertyHelperException
     */
    public static String getPathProperty(final Properties properties, String key, boolean isDir, boolean canRead, boolean canWrite)
            throws PropertyHelperException {
        String strProperty = getStrProperty(properties, key);
        File dir = new File(strProperty);
        if (!dir.exists() || (isDir && !dir.isDirectory()) || (!isDir && !dir.isFile())) {
            throw new PropertyHelperException(String.format("Path %s for property %s does not exist.", strProperty, key));
        }
        if (canRead && !dir.canRead()) {
            throw new PropertyHelperException(String.format("Does not have rights to read %s for property %s.", strProperty, key));
        }
        if (canWrite && !dir.canWrite()) {
            throw new PropertyHelperException(String.format("Does not have rights to write into %s for property %s.", strProperty, key));
        }
        return strProperty;
    }
}
