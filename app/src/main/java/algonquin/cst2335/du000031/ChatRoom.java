package algonquin.cst2335.du000031;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import algonquin.cst2335.du000031.data.ChatRoomViewModel;
import algonquin.cst2335.du000031.databinding.ActivityChatRoomBinding;
import algonquin.cst2335.du000031.databinding.ReceiveMessageBinding;
import algonquin.cst2335.du000031.databinding.SentMessageBinding;

public class ChatRoom extends AppCompatActivity {
    private ActivityChatRoomBinding binding;
    private ArrayList<ChatMessage> messages;
    private ChatRoomViewModel chatModel;
    private RecyclerView.Adapter myAdapter;
    private ChatMessageDAO mDAO;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.item_delete:
                //int position = getAbsoluteAdapterPosition();
                ChatMessage clickedMessage = chatModel.selectedMessage.getValue();
                if (clickedMessage == null) {
                    break;
                }
                int position = messages.indexOf(clickedMessage);

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoom.this);
                builder.setMessage("Do you want to delete the message: " + clickedMessage.getMessage())
                        .setTitle("Question:")
                        .setPositiveButton("Yes", (dialog, cl) -> {
//                            ChatMessage clickedMessage = messages.get(position);

                            Executor thread = Executors.newSingleThreadExecutor();
                            thread.execute(() -> {
                                mDAO.deleteMessage(clickedMessage);
                                messages.remove(position);
                                runOnUiThread(() -> {
                                    myAdapter.notifyItemRemoved(position);
                                    // close the fragment
                                    getSupportFragmentManager().popBackStack();
                                    // set selected message to null
                                    chatModel.selectedMessage.postValue(null);
                                });
                            });

//                            Snackbar.make(messageText, "You deleted message #" + position, Snackbar.LENGTH_LONG)
//                                    .setAction("Undo", clk -> {
//                                        Executor newThread = Executors.newSingleThreadExecutor();
//                                        newThread.execute(() -> {
//                                            mDAO.insertMessage(clickedMessage);
//                                            messages.add(position, clickedMessage);
//                                            runOnUiThread(() -> myAdapter.notifyItemInserted(position));
//                                        });
//                                    })
//                                    .show();
                        })
                        .setNegativeButton("No", (dialog, cl) -> {})
                        .create().show();
                break;
            case R.id.item_about:
                Toast.makeText(getApplicationContext(), "Version 1.0, created by Dongni Du", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MessageDatabase db = Room.databaseBuilder(getApplicationContext(), MessageDatabase.class, "database-name").build();
        mDAO = db.cmDAO();

        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.myToolbar);
        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        messages = chatModel.messages.getValue();
        if (messages == null) {
            //chatModel.messages.postValue(messages = new ArrayList<ChatMessage>());
            chatModel.messages.setValue(messages = new ArrayList<>());

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                messages.addAll(mDAO.getAllMessages());
                runOnUiThread(() -> binding.recycleView.setAdapter(myAdapter));
            });
        }
        setContentView(binding.getRoot());

        binding.sendButton.setOnClickListener(btn -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");
            String currentDateAndTime = sdf.format(new Date());

            ChatMessage newMessage = new ChatMessage(binding.textInput.getText().toString(), currentDateAndTime, true);
            messages.add(newMessage);

            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                long id = mDAO.insertMessage(newMessage);
                newMessage.id = id;

                runOnUiThread(() -> myAdapter.notifyItemInserted(messages.size() - 1));
            });

            //myAdapter.notifyItemInserted(messages.size() - 1);
            //clear the previous text
            binding.textInput.setText("");
        });

        binding.receiveButton.setOnClickListener(btn -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");
            String currentDateAndTime = sdf.format(new Date());

            ChatMessage newMessage = new ChatMessage(binding.textInput.getText().toString(), currentDateAndTime, false);
            messages.add(newMessage);
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() -> {
                long id = mDAO.insertMessage(newMessage);
                newMessage.id = id;

                runOnUiThread(() -> myAdapter.notifyItemInserted(messages.size() - 1));
            });
            //myAdapter.notifyItemInserted(messages.size() - 1);
            binding.textInput.setText("");
        });

        myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == 0) {
                    SentMessageBinding binding = SentMessageBinding.inflate(getLayoutInflater(), parent, false);
                    return new MyRowHolder(binding.getRoot());
                } else {
                    ReceiveMessageBinding binding = ReceiveMessageBinding.inflate(getLayoutInflater(), parent, false);
                    return new MyRowHolder(binding.getRoot());
                }
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                String message = messages.get(position).getMessage();
                holder.messageText.setText(message);
                String time = messages.get(position).getTimeSent();
                holder.timeText.setText(time);
            }

            @Override
            public int getItemCount() {
                return messages.size();
            }

            @Override
            public int getItemViewType(int position) {
                if (messages.get(position).isSentButton()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        };

        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));

        chatModel.selectedMessage.observe(this, newMessageValue -> {
            // make sure the newMessageValue is not null
            if (newMessageValue != null) {
                MessageDetailsFragment chatFragment = new MessageDetailsFragment(newMessageValue);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentLocation, chatFragment)
                        .addToBackStack("")
                        .commit();
            }
        });
    }

    class MyRowHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        public MyRowHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(click -> {
                /*
                int position = getAbsoluteAdapterPosition();

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoom.this);
                builder.setMessage("Do you want to delete the message: " + messageText.getText())
                        .setTitle("Question:")
                        .setPositiveButton("Yes", (dialog, cl) -> {
                            ChatMessage clickedMessage = messages.get(position);

                            Executor thread = Executors.newSingleThreadExecutor();
                            thread.execute(() -> {
                                mDAO.deleteMessage(clickedMessage);
                                messages.remove(position);
                                runOnUiThread(() -> myAdapter.notifyItemRemoved(position));
                            });

                            Snackbar.make(messageText, "You deleted message #" + position, Snackbar.LENGTH_LONG)
                                    .setAction("Undo", clk -> {
                                        Executor newThread = Executors.newSingleThreadExecutor();
                                        newThread.execute(() -> {
                                            mDAO.insertMessage(clickedMessage);
                                            messages.add(position, clickedMessage);
                                            runOnUiThread(() -> myAdapter.notifyItemInserted(position));
                                        });
                                    })
                                    .show();
                        })
                        .setNegativeButton("No", (dialog, cl) -> {})
                        .create().show();
                */
                int position = getAbsoluteAdapterPosition();
                ChatMessage selected = messages.get(position);
                chatModel.selectedMessage.postValue(selected);
            });

            messageText = itemView.findViewById(R.id.message);
            timeText = itemView.findViewById(R.id.time);
        }
    }
}