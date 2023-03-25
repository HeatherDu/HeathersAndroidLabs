package algonquin.cst2335.du000031;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import algonquin.cst2335.du000031.databinding.ActivityMainBinding;

/** This is a page that simulates a login page
 * @author Dongni Du
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    protected String cityName;
    protected RequestQueue queue = null;
    String iconName = null;
    ImageRequest imgReq;
    Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getForecast.setOnClickListener(btn -> {
            cityName = binding.cityTextField.getText().toString();
            String stringUrl = null;
            try {
                stringUrl = "https://api.openweathermap.org/data/2.5/weather?q="
                        + URLEncoder.encode(cityName, "UTF-8")
                        + "&appid=7e943c97096a9784391a981c4d878b22&units=metric";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, stringUrl, null,
                    (response) -> {
                        try {
                            JSONArray weatherArray = response.getJSONArray("weather");
                            JSONObject position0 = weatherArray.getJSONObject(0);
                            iconName = position0.getString("icon");

                            String pathname = getFilesDir() + "/" + iconName + ".png";
                            File file = new File(pathname);
                            if (file.exists()) {
                                image = BitmapFactory.decodeFile(pathname);
                                binding.icon.setImageBitmap(image);
                            } else {
                                imgReq = new ImageRequest("https://openweathermap.org/img/w/" + iconName + ".png", new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap response) {
                                        FileOutputStream fOut = null;
                                        try {
                                            fOut = openFileOutput(iconName + ".png", Context.MODE_PRIVATE);
                                            image = response;
                                            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                            binding.icon.setImageBitmap(image);
                                            fOut.flush();
                                            fOut.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 1024, 1024, ImageView.ScaleType.CENTER, null, (error -> {}));
                                queue.add(imgReq);
                            }

                            JSONObject mainObject = response.getJSONObject("main");
                            double current = mainObject.getDouble("temp");
                            double max = mainObject.getDouble("temp_max");
                            double min = mainObject.getDouble("temp_min");
                            int humidity = mainObject.getInt("humidity");
                            String description = position0.getString("description");

                            runOnUiThread(() -> {
                                binding.temp.setText("The current temperature is " + current);
                                binding.temp.setVisibility(View.VISIBLE);
                                binding.maxTemp.setText("The max temperature is " + max);
                                binding.maxTemp.setVisibility(View.VISIBLE);
                                binding.minTemp.setText("The min temperature is " + min);
                                binding.minTemp.setVisibility(View.VISIBLE);
                                binding.humidity.setText("The humidity is " + humidity + "%");
                                binding.humidity.setVisibility(View.VISIBLE);
                                binding.description.setText(description);
                                binding.description.setVisibility(View.VISIBLE);

                                binding.icon.setVisibility(View.VISIBLE);
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    (error) -> {});

            queue.add(request);
        });
    }

    /** This function scans the string to see if it is complex enough
     *
     * @param pw the string to verify
     * @return Returns true if password is complex enough, otherwise return false
     */
    public boolean checkPasswordComplexity(String pw) {
        boolean foundUpperCase, foundLowerCase, foundNumber, foundSpecial;
        foundUpperCase = foundLowerCase = foundNumber = foundSpecial = false;
        for (int i = 0; i < pw.length(); i++) {
            char c = pw.charAt(i);
            if (Character.isUpperCase(c)) {
                foundUpperCase = true;
            }
            if (Character.isLowerCase(c)) {
                foundLowerCase = true;
            }
            if (Character.isDigit(c)) {
                foundNumber = true;
            }
            if (isSpecialCharacter(c)) {
                foundSpecial = true;
            }
        }
        if (!foundUpperCase) {
            Toast.makeText(this, "Password is missing an upper case letter", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!foundLowerCase) {
            Toast.makeText(this, "Password is missing a lower case letter", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!foundNumber) {
            Toast.makeText(this, "Password is missing a number", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!foundSpecial) {
            Toast.makeText(this, "Password is missing a special character", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    /** This function checks if the character is a special character
     *
     * @param c the character to check
     * @return Returns true if the character is a special character, otherwise return false
     */
    public boolean isSpecialCharacter(char c) {
        switch (c) {
            case '#':
            case '$':
            case '%':
            case '^':
            case '&':
            case '*':
            case '!':
            case '@':
            case '?':
                return true;
            default:
                return false;
        }
    }
}