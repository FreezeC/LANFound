package frezc.lanfoundandconnection.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{
    private EditText editText;
    private Button button;
    private FrameLayout container;
    private SearcherFragment searcherFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.et_name);
        button = (Button) findViewById(R.id.btn_start);
        button.setOnClickListener(this);
        container = (FrameLayout) findViewById(R.id.container);
    }


    @Override
    public void onClick(View v) {
        String s = editText.getText().toString();
        if(s.length() <= 0 || s.length() > 10){
            Toast.makeText(this, "Name length is available between 1~10", Toast.LENGTH_SHORT).show();
        }else {
            if(searcherFragment == null){
                searcherFragment = SearcherFragment.newInstance(s);
                editText.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, searcherFragment)
                        .commit();
            }
        }
    }
}
