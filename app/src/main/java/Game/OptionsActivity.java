package Game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.labirynt2.R;

public class OptionsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        Spinner levelSpinner = findViewById((R.id.spinner_level));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);
//  Wczytuję aktualną pozycję spinnera poziomu trudności gry.
        prefs = this.getSharedPreferences("game", Context.MODE_PRIVATE);
        String lv=prefs.getString("level", "Łatwy");
        int spinnerPosition = adapter.getPosition(lv);
        levelSpinner.setSelection(spinnerPosition);
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLUE);
                ((TextView) parent.getChildAt(0)).setTextSize(25);
                String text = parent.getItemAtPosition(position).toString();
                prefs = getSharedPreferences("game", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("level", text);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner SteeringSpinner = findViewById((R.id.spinner_sterowanie));
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.steering, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SteeringSpinner.setAdapter(adapter2);
//  Wczytuję aktualną pozycję spinnera sposobu sterowania.
        lv=prefs.getString("steering", "Akcelerometr");
        spinnerPosition = adapter2.getPosition(lv);
        SteeringSpinner.setSelection(spinnerPosition);
        SteeringSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLUE);
                ((TextView) parent.getChildAt(0)).setTextSize(25);
                String text = parent.getItemAtPosition(position).toString();
                prefs = getSharedPreferences("game", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("steering", text);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
