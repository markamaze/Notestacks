package com.notestacks;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private String selected_notestack_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.activity_main_actionbar));
        registerForContextMenu( (LinearLayout)findViewById( R.id.notestack_list_view ) );

        inflateNotestackList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.actionbar_menu_main, menu);
        getMenuInflater().inflate(R.menu.notestacks_list, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(!menu.hasVisibleItems())
            getMenuInflater().inflate(R.menu.notestack_list_context_menu, menu);
        if(this.selected_notestack_path == null) this.selected_notestack_path = v.getTag().toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.about: return true;
            case R.id.create_stack: createNewNotestack();
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void createNewNotestack() {
        try {
            Notestack newNotestack = new Notestack(getFilesDir().getPath());
            newNotestack.writeToFile();
            recreate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void inflateNotestackList() {
        File[] files = getFilesDir().listFiles();
        LinearLayout notestack_list = (LinearLayout)findViewById(R.id.notestack_list_view);
        for(File file : files){
            Notestack notestack = new Notestack(file.getPath());
            final TextView list_item = (TextView) getLayoutInflater().inflate(R.layout.view_notestack_item, null);
            list_item.setText(notestack.getTitle());
            list_item.setTag(notestack.getPath());
            list_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    registerForContextMenu(list_item);
                    return false;
                }
            });
            notestack_list.addView(list_item);
        }
    }

    public void openNotestack(View view){
        Intent intent = new Intent( this, NotestackActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("notestackPath", view.getTag().toString());
        startActivity(intent);
    }

    public void renameNotestack(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );

        final EditText input = new EditText( this );
        input.setInputType( InputType.TYPE_CLASS_TEXT );
        builder.setView( input );
        builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Notestack notestack = new Notestack( MainActivity.this.selected_notestack_path );
                notestack.setTitle( input.getText().toString() );
                notestack.writeToFile();
                recreate();
            }
        } );
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void deleteNotestack(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final TextView input = (TextView) getLayoutInflater().inflate(R.layout.dialog_layout_confirm_delete, null);
        input.setText( "Are you sure you want to delete this Notestack?" );
        builder.setView(input);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                File foundFile = new File(MainActivity.this.selected_notestack_path);
                foundFile.delete();
                recreate();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
