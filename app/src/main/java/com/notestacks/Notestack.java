package com.notestacks;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class Notestack {
    private int current_note_index;
    private UUID notestack_id; //this may be unused with how I've organized the notes property
    private String notestack_title;
    private String notestack_path;
    private ArrayList<Note> notes;

    public Notestack(String notestack_location){
        File foundFile = new File(notestack_location);
        if(foundFile.exists() && !foundFile.isDirectory()) readFromFile(notestack_location);
        else {
            this.notestack_id = UUID.randomUUID();
            this.setPath(notestack_location + "/" + this.notestack_id);
            this.setTitle( "New Notestack" );
            this.notes = new ArrayList<Note>();
            this.notes.add(0, new Note());
            this.setCurrentNoteIndex( this.getStackSize() );
        }

    }
    public boolean writeToFile() {
        try {
            File notestack_file = new File(this.getPath());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(this.writeDocument());
            StreamResult streamResult = new StreamResult(notestack_file);
            transformer.transform(source, streamResult);
            return true;
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
            return false;
        } catch (TransformerException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void readFromFile (String path){
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            File file = new File(path);
            Document document = documentBuilder.parse(file);
            readDocument(document.getDocumentElement(), path);
        } catch (Exception e){ e.printStackTrace(); }

    }

    public Document writeDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element element = document.createElement("notestack");

        for(int index = 0; index < getStackSize(); index++){
            Node note_element = this.notes.get( index ).writeDocument(document, index);
            element.appendChild( note_element );

        }
        document.appendChild(element);


        Attr id_attribute = document.createAttribute("id");
        id_attribute.setValue(this.notestack_id.toString());
        element.setAttributeNode(id_attribute);

        Attr title_attribute = document.createAttribute("title");
        title_attribute.setValue(this.notestack_title);
        element.setAttributeNode(title_attribute);

        Attr current_index = document.createAttribute( "current_index" );
        current_index.setValue( String.valueOf( this.getCurrentNoteIndex() ) );
        element.setAttributeNode(current_index);

        return document;
    }
    private void readDocument(Element notestack_element, String path) {
        this.notestack_id = UUID.fromString( notestack_element.getAttribute( "id" ) );
        this.setTitle( notestack_element.getAttribute( "title" ) );
        this.setPath( path );
        String index = notestack_element.getAttribute( "current_index" );

        NodeList document_nodes = notestack_element.getElementsByTagName( "note" );
        int nodes_length = document_nodes.getLength();

        if(nodes_length > 0) {
            this.notes = new ArrayList<Note>(nodes_length);
            for (int i = 0; i < document_nodes.getLength(); i++) {
                Element note_element = (Element) document_nodes.item( i );
                Note note = new Note( note_element );
                int note_index = Integer.parseInt( note_element.getAttribute( "index" ) );
                if (note_index >= 0) this.notes.add( i, note );
                else this.notes.add( note );
            }
        }
        else {
            this.notes = new ArrayList<Note>();
            this.insertNote( new Note(), 0 );
        }
        setCurrentNoteIndex( Integer.parseInt( index ) );

    }

    public String getPath() { return this.notestack_path; }
    private void setPath(String path) { this.notestack_path = path; }

    public String getTitle() { return this.notestack_title; }
    public void setTitle(String title) { this.notestack_title = title; }

    public int getStackSize() {
        if(this.notes == null || this.notes.size() < 0) this.insertNote( new Note(), 0 );
        return this.notes.size();
    }
    public Note getCurrentNote(){
        if(this.notes == null || this.notes.isEmpty()) insertNote( new Note(), 0 );
        return this.notes.get(this.getCurrentNoteIndex()); }
    public int getCurrentNoteIndex() {
        if (this.current_note_index > this.getStackSize() - 1) this.setCurrentNoteIndex( this.getStackSize() - 1 );
        else if (this.current_note_index < 0) this.setCurrentNoteIndex( 0 );
        return this.current_note_index;
    }


    public void insertNote(Note note, int index){
        if(this.notes == null) this.notes = new ArrayList<Note>();
        this.notes.add( index, note );
        this.setCurrentNoteIndex( index );
    }
    public void removeCurrentNote(){
        this.notes.remove( this.getCurrentNoteIndex() );
    }
    public void setCurrentNoteIndex(int index) {
        if( index == this.current_note_index) return;
        if(index < 0) this.current_note_index = 0;
        else if(index > this.getStackSize() - 1) this.current_note_index = this.getStackSize() - 1;
        else this.current_note_index = index;

//        if(this.current_note_index != index) writeToFile();
    }
    public void shiftCurrentNoteIndexUp(){ setCurrentNoteIndex( (getCurrentNoteIndex() + 1) ); }
    public void shiftCurrentNoteIndexDown(){ setCurrentNoteIndex( (getCurrentNoteIndex() - 1) ); }

    public void moveCurrentNote(int new_index) {
        Note currentNote = this.notes.remove( getCurrentNoteIndex() );
        this.notes.add(new_index, currentNote);
        setCurrentNoteIndex( new_index );
    }
}
