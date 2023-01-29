package algonquin.cst2335.du000031.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.Toast;

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

        model.isSelected.observe(this, selected -> {
            binding.myCheckbox.setChecked(selected);
            binding.mySwitch.setChecked(selected);
            binding.myRadioButton.setChecked(selected);

            Toast.makeText(this, "This value is now: " + selected, Toast.LENGTH_SHORT).show();
        });

        binding.myButton.setOnClickListener(v -> {
            model.editString.postValue(binding.myEditText.getText().toString());
        });

        binding.myCheckbox.setOnCheckedChangeListener((btn, isChecked) -> {
            model.isSelected.postValue(isChecked);
        });

        binding.mySwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            model.isSelected.postValue(isChecked);
        });

        binding.myRadioButton.setOnCheckedChangeListener((btn, isChecked) -> {
            model.isSelected.postValue(isChecked);
        });

        binding.myImageButton.setOnClickListener(v -> {
            Toast.makeText(this,
                    "The width = " + binding.myImageButton.getWidth() + " and height = " + binding.myImageButton.getHeight(),
                    Toast.LENGTH_SHORT).show();
        });
    }
}