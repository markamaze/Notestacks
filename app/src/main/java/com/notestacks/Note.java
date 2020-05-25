package com.notestacks;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;


public class Note {
//    private UUID note_id;
    private String note_text;

    public Note(Element note_xml_element){ readDocument(note_xml_element); }
    public Note(){
//        this.note_id = UUID.randomUUID();
        this.note_text = "";

        //how do I
    }
//    public UUID getId() { return this.note_id; }
    public void setNoteText(String text) { this.note_text = text; }
    public String getNoteText() { return this.note_text; }

    public Node writeDocument(Document document, int index) throws ParserConfigurationException {
        Element element = document.createElement("note");
        element.setTextContent( this.getNoteText() );
//        element.setAttribute(  "id", this.getId().toString() );
        element.setAttribute( "index", String.valueOf( index ) );
        return element;
    }
    public boolean readDocument(Element note_xml_element) {
        if(note_xml_element == null) return true;
//        this.note_id = UUID.fromString( note_xml_element.getAttribute( "id" ) );
        this.note_text = note_xml_element.getTextContent();

        return true;
    }

}
