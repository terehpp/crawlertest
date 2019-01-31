package org.terehpp.crawler.model;

import org.terehpp.crawler.component.analyzer.XMLDateTimeAdapter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Date;

/**
 * Model of Entry.
 */
@XmlRootElement(name = "Entry")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "Entry")
public class Entry implements Serializable, DbEntity {
    @Id
    private Long id;
    @XmlElement(name = "content", required = true)
    @Column(name = "content")
    private String content;
    @XmlJavaTypeAdapter(XMLDateTimeAdapter.class)
    @XmlElement(name = "creationDate", required = true)
    @Column(name = "creationDate")
    private Date creationDate;

    public Entry() {
    }

    public Entry(Long id, String content, Date date) {
        this.id = id;
        this.content = content;
        this.creationDate = date;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
