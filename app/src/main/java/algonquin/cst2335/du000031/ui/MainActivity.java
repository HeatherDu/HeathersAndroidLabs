package algonquin.cst2335.du000031.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import algonquin.cst2335.du000031.data.MainViewModel;
import algonquin.cst2335.du000031.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(this).get(MainViewModel.class);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model.editString.observe(this, s -> {
            binding.myText.setText("Your edit text has: " + s);
        });

        binding.myButton.setOnClickListener(v -> {
            model.editString.postValue(binding.myEditText.getText().toString());
        });
    }
}