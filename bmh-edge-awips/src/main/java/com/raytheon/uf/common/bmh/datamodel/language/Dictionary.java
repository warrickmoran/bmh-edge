/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.bmh.datamodel.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.raytheon.uf.common.bmh.diff.DiffString;
import com.raytheon.uf.common.bmh.diff.DiffTitle;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;

/**
 * Record for storing a dictionary, which is basically a language and collection
 * of words.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2014 3175       rjpeter     Initial creation
 * Jul 08, 2014 3302       bkowal      Use eager fetching to eliminate session closed
 * Jul 08, 2014 3355       mpduff      Updated mappings between dictionary and words
 * Jul 29, 2014 3407       mpduff      Removed orphanRemoval from words field, added toString()
 * Aug 04, 2014 3175       rjpeter     Added serialization adapter to fix circular reference.
 * Oct 16, 2014 3636       rferrel     Added logging.
 * Oct 21, 2014 3746       rjpeter     Hibernate upgrade.
 * Dec 11, 2014 3618       bkowal      Added {@link #GET_NATIONAL_DICTIONARIES}.
 * Dec 15, 2014 3618       bkowal      Added {@link #GET_NATIONAL_DICTIONARY_FOR_LANGUAGE}.
 * Dec 16, 2014 3618       bkowal      Added {@link #GET_NON_NATIONAL_DICTIONARIES_FOR_LANGUAGE}.
 * Jan 06, 2015 3931       bkowal      Added XML JAXB Marshaling tags.
 * May 12, 2015 4248       rjpeter     Remove bmh schema, standardize foreign/unique keys.
 * Dec 03, 2015 5159       bkowal      Added {@link #getAllWordsAsStrings()}.
 * </pre>
 * 
 * @author rjpeter
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = Dictionary.GET_DICTIONARY_NAMES_QUERY, query = "select dict.name from Dictionary dict"),
        @NamedQuery(name = Dictionary.GET_NATIONAL_DICTIONARIES, query = Dictionary.GET_NATIONAL_DICTIONARIES_QUERY),
        @NamedQuery(name = Dictionary.GET_NATIONAL_DICTIONARY_FOR_LANGUAGE, query = Dictionary.GET_NATIONAL_DICTIONARY_FOR_LANGUAGE_QUERY),
        @NamedQuery(name = Dictionary.GET_NON_NATIONAL_DICTIONARIES_FOR_LANGUAGE, query = Dictionary.GET_NON_NATIONAL_DICTIONARIES_FOR_LANGUAGE_QUERY) })
@Entity
@Table(name = "dictionary")
@XmlRootElement(name = "bmhDictionary")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = DictionaryAdapter.class)
public class Dictionary {

    public static final String GET_DICTIONARY_NAMES_QUERY = "getDictionaryNames";

    public static final String GET_NATIONAL_DICTIONARIES = "getNationalDictionaries";

    protected static final String GET_NATIONAL_DICTIONARIES_QUERY = "FROM Dictionary d WHERE d.national = true";

    public static final String GET_NATIONAL_DICTIONARY_FOR_LANGUAGE = "getNationalDictionariesForLanguage";

    protected static final String GET_NATIONAL_DICTIONARY_FOR_LANGUAGE_QUERY = "FROM Dictionary d WHERE d.language = :language AND d.national = true";

    public static final String GET_NON_NATIONAL_DICTIONARIES_FOR_LANGUAGE = "getNonNationalDictionariesForLanguage";

    protected static final String GET_NON_NATIONAL_DICTIONARIES_FOR_LANGUAGE_QUERY = "FROM Dictionary d WHERE d.language = :language AND d.national = false";

    @Id
    @Column(length = 20)
    @DiffString
    @DiffTitle(position = 1)
    @XmlAttribute
    private String name = null;

    @Enumerated(EnumType.STRING)
    @Column(length = 7, nullable = false)
    @XmlAttribute
    private Language language = Language.ENGLISH;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dictionary", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @XmlElement(name = "word")
    private Set<Word> words;

    /**
     * boolean indicating whether or not this dictionary is the national
     * dictionary. There can only be one national dictionary per
     * {@link Language}. The national dictionary is used in every message
     * transformation. However, {@link Word} s in the dictionary can be
     * overridden by dictionaries assigned to individual voices and
     * transmitters.
     */
    @Column(nullable = false)
    private boolean national = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Set<Word> getWords() {
        if (words == null) {
            words = new HashSet<Word>();
        }
        return words;
    }

    public void setWords(Set<Word> words) {
        this.words = words;
        if ((words != null) && !words.isEmpty()) {
            for (Word word : words) {
                word.setDictionary(this);
            }
        }
    }

    /**
     * @return the national
     */
    public boolean isNational() {
        return national;
    }

    /**
     * @param national
     *            the national to set
     */
    public void setNational(boolean national) {
        this.national = national;
    }

    public boolean containsWord(String wordName) {
        if (words != null) {
            for (Word word : words) {
                if (wordName.equalsIgnoreCase(word.getWord())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get a {@link Word} from the dictionary.
     * 
     * @param wordName
     *            The word name
     * @return The Word or null if no word
     */
    public Word getWord(String wordName) {
        if (words != null) {
            for (Word word : words) {
                if (wordName.equalsIgnoreCase(word.getWord())) {
                    return word;
                }
            }
        }

        return null;
    }

    /**
     * Returns a {@link List} of {@link String}s consisting of all of the
     * {@link Word#getWord()}.
     * 
     * @return a {@link List} of {@link String}s.
     */
    public List<String> getAllWordsAsStrings() {
        if (words == null || words.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> wordList = new ArrayList<>(words.size());
        for (Word word : words) {
            wordList.add(word.getWord());
        }
        return wordList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Dictionary [name=" + name + ", language=" + language + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Dictionary other = (Dictionary) obj;
        if (language != other.language) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
