package com.notestacks;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

public class NotestackActivity extends AppCompatActivity {
    private Notestack notestack;
    private Note currentNote;
    private boolean noteViewIsEditable;
    OnSwipeTouchListener noteViewTouchListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_notestack );
        initializeModelData();
        initializeToolbars();
        initializeNoteView();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        noteViewTouchListener.getGestureDetector().onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public void onBackPressed() {
        if(this.noteViewIsEditable) {
            this.noteViewIsEditable = false;
            showReadableNoteView();
        }
        else super.onBackPressed();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_main, menu);
        getMenuInflater().inflate(R.menu.notestack_activity_menu, menu);
        return super.onCreateOptionsMenu( menu );
    }


    private void initializeModelData(){
        String notestackPath = getIntent().getStringExtra( "notestackPath" );
        this.notestack = new Notestack( notestackPath );
    }
    private void initializeNoteView(){
        this.noteViewTouchListener = new OnSwipeTouchListener(NotestackActivity.this) {
            public void onSwipeRight() { NotestackActivity.this.loadPreviousNote(null); }
            public void onSwipeLeft() { NotestackActivity.this.loadNextNote(null); }
            public void onDblTap() { NotestackActivity.this.showWritableNoteView(); }
            public void onSwipeBottom() { NotestackActivity.this.showNoteTools(); }
            public void onSwipeTop() { NotestackActivity.this.hideNoteTools(); }
        };
        this.noteViewIsEditable = false;
        setCurrentNoteView();
    }

    private void showNoteTools() {
//        Toolbar toolbox = findViewById( R.id.activity_notestack_toolbox );
//        getLayoutInflater().inflate( R.layout.notestack_toolbox, toolbox );
System.out.println( "jj" );
    }

    private void hideNoteTools() {
//        Toolbar toolbox = findViewById( R.id.activity_notestack_toolbox );
        System.out.println( "jj" );


    }

    private void initializeToolbars() {
        setSupportActionBar( (Toolbar) findViewById( R.id.activity_notestack_actionbar ) );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setTitle( this.notestack.getTitle() );

        ViewGroup navbar_view = (ViewGroup) findViewById( R.id.activity_notestack_navbar );
        getLayoutInflater().inflate( R.layout.notestack_navbar, navbar_view );

        ViewGroup toolbox_view = (ViewGroup) findViewById( R.id.activity_notestack_toolbox );
        getLayoutInflater().inflate( R.layout.notestack_toolbox, toolbox_view );
    }


    private void setCurrentNoteView(){
        ViewGroup sceneRoot = findViewById( R.id.activity_notestack_current_note_view );
        Slide slideTransition = new Slide( Gravity.TOP );
        TransitionManager.beginDelayedTransition( sceneRoot, slideTransition );

        this.currentNote = this.notestack.getCurrentNote();
        this.notestack.writeToFile();
        setNoteHeader();
        showReadableNoteView();
    }
    private void showReadableNoteView() {
        ScrollView noteViewer = (ScrollView) findViewById( R.id.activity_notestack_current_note_view );
        final TextView noteText = (TextView) getLayoutInflater().inflate( R.layout.display_note, null);
        noteText.setText( this.currentNote.getNoteText() );
        noteText.setOnTouchListener( this.noteViewTouchListener );
        noteViewer.removeAllViews();
        noteViewer.addView( noteText );
        this.noteViewIsEditable = false;
        setNoteHeader();
    }
    private void showWritableNoteView() {
        ScrollView noteViewer = (ScrollView) findViewById( R.id.activity_notestack_current_note_view );
        final EditText noteEditor = (EditText) getLayoutInflater().inflate( R.layout.edit_note, null);
        noteEditor.setText( this.currentNote.getNoteText() );
        noteEditor.setOnTouchListener( this.noteViewTouchListener );
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                NotestackActivity.this.currentNote.setNoteText( s.toString() );
                NotestackActivity.this.notestack.writeToFile();
            }
        };
        noteEditor.addTextChangedListener(textWatcher);
        noteViewer.removeAllViews();
        noteViewer.addView( noteEditor );
        this.noteViewIsEditable = true;
        setNoteHeader();
    }
    private void setNoteHeader(){
        String subtitle = "note " + String.valueOf(this.notestack.getCurrentNoteIndex() + 1) + " of " + String.valueOf( this.notestack.getStackSize() );

        TextView header = (TextView) findViewById( R.id.note_header_view );
        header.setText( subtitle );
        header.setTextSize( 20 );
    }


    public void loadPreviousNote(View view){
        this.notestack.shiftCurrentNoteIndexDown();
        setCurrentNoteView();
    }
    public void loadNextNote(View view){
        this.notestack.shiftCurrentNoteIndexUp();
        setCurrentNoteView();
    }
    public void loadTopNote(View view){
        this.notestack.setCurrentNoteIndex( this.notestack.getStackSize() - 1 );
        setCurrentNoteView();
    }
    public void loadBottomNote(View view){
        this.notestack.setCurrentNoteIndex( 0 );
        setCurrentNoteView();
    }


    public void openAddNoteMenu(View view) {
        PopupMenu popup = new PopupMenu( this, (Button) findViewById( R.id.addNoteButton ) );
        getMenuInflater().inflate(R.menu.add_note_popup, popup.getMenu());
        popup.show();
    }
    public void addNoteToTop(MenuItem menuItem){
        this.notestack.insertNote( new Note(), this.notestack.getStackSize() );
        this.noteViewIsEditable = true;
        setCurrentNoteView();
    }
    public void addPreviousNote(MenuItem menuItem){
        this.notestack.insertNote( new Note(), this.notestack.getCurrentNoteIndex() );
        this.noteViewIsEditable = true;
        setCurrentNoteView();
    }
    public void addNextNote(MenuItem menuItem){
        this.notestack.insertNote( new Note(), this.notestack.getCurrentNoteIndex() + 1 );
        this.noteViewIsEditable = true;
        setCurrentNoteView();
    }
    public void addNoteToBottom(MenuItem menuItem){
        this.notestack.insertNote( new Note(), 0 );
        this.noteViewIsEditable = true;
        setCurrentNoteView();
    }


    public void openMoveNoteMenu(View view) {
        PopupMenu popup = new PopupMenu( this, (Button) findViewById( R.id.moveNoteButton ) );
        getMenuInflater().inflate(R.menu.move_note_popup, popup.getMenu());
        popup.show();
    }
    public void moveNoteToTop(MenuItem menuItem){
        this.notestack.moveCurrentNote(this.notestack.getStackSize() -1 );
        setCurrentNoteView();
    }
    public void moveNoteDown(MenuItem menuItem){
        this.notestack.moveCurrentNote(this.notestack.getCurrentNoteIndex() - 1 );
        setCurrentNoteView();
    }
    public void moveNoteUp(MenuItem menuItem){
        this.notestack.moveCurrentNote(this.notestack.getCurrentNoteIndex() + 1 );
        setCurrentNoteView();
    }
    public void moveNoteToBottom(MenuItem menuItem){
        this.notestack.moveCurrentNote(0);
        setCurrentNoteView();
    }


    public void deleteCurrentNote(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final TextView input = (TextView) getLayoutInflater().inflate(R.layout.dialog_layout_confirm_delete, null);
        input.setText( "Are you sure you want to delete this note?" );
        builder.setView(input);

        DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NotestackActivity.this.notestack.removeCurrentNote();
//                NotestackActivity.this.notestack.writeToFile();
                NotestackActivity.this.setCurrentNoteView();
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { dialog.cancel(); }
        };

        builder.setPositiveButton("Confirm", confirmListener);
        builder.setNegativeButton("Cancel", cancelListener);

        builder.show();
    }
}