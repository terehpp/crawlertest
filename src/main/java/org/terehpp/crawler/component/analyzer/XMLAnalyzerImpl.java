package org.terehpp.crawler.component.analyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.terehpp.crawler.model.DbEntity;
import org.terehpp.crawler.utils.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Xml file analyzer.
 *
 * @param <T> Entity type.
 */
public class XMLAnalyzerImpl<T extends DbEntity> implements Analyzer<T> {
    private final static Log logger = LogFactory.getLog(XMLAnalyzerImpl.class);
    private final Class<T> type;
    private final String xsdSchemaFile;
    private static ThreadLocal<Unmarshaller> unmarshaller = new ThreadLocal<>();
    private static ThreadLocal<Schema> schema = new ThreadLocal<>();

    /**
     * Constructor.
     *
     * @param analyzedType Entity type to parse.
     */
    public XMLAnalyzerImpl(Class<T> analyzedType) {
        type = analyzedType;
        xsdSchemaFile = null;
    }

    /**
     * Constructor.
     *
     * @param analyzedType Entity type to parse.
     * @param xsdFile      XSD file to validate schema.
     */
    public XMLAnalyzerImpl(Class<T> analyzedType, String xsdFile) {
        type = analyzedType;
        xsdSchemaFile = xsdFile;
    }

    /**
     * Analyze.
     *
     * @param file File.
     * @param id   Entity identifier.
     * @return Entity.
     */
    @Override
    public AnalyzerResult<T> analyze(String file, Long id) {
        try {
            Unmarshaller um = getUnmarshaller();
            getSchema().ifPresent(schema -> {
                um.setSchema(schema);
            });
            try (FileInputStream fs = new FileInputStream(file)) {
                T obj = (T) um.unmarshal(fs);
                if (obj == null) {
                    return new AnalyzerResult<T>(true,
                            String.format("Can not deserialize file %s", file), null);
                }
                obj.setId(id);
                return new AnalyzerResult<T>(false, null, obj);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return new AnalyzerResult<T>(true,
                        String.format("Can not analyze file %s", file), null);
            }
        } catch (JAXBException | SAXException e) {
            logger.error(e.getMessage(), e);
            return new AnalyzerResult<T>(true,
                    String.format("Can not create Unmarshaller for type %s", type.getSimpleName()), null);
        }
    }

    /**
     * Get unmarshaller for current thread.
     *
     * @return Unmarshaller.
     * @throws JAXBException
     */
    private Unmarshaller getUnmarshaller() throws JAXBException {
        if (unmarshaller.get() == null) {
            JAXBContext context = JAXBContext.newInstance(type);
            unmarshaller.set(context.createUnmarshaller());
        }
        return unmarshaller.get();
    }

    /**
     * Get schema.
     *
     * @return Schema.
     * @throws SAXException
     */
    private Optional<Schema> getSchema() throws SAXException {
        if (schema.get() == null && StringUtils.isNotBlank(xsdSchemaFile)) {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            schema.set(sf.newSchema(new File(xsdSchemaFile)));
        }
        return Optional.of(schema.get());
    }
}
