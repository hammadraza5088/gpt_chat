package com.example.gpt_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.IllformedLocaleException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageList= new ArrayList<>();
recyclerView=findViewById(R.id.recycler_view);
        welcomeTextView=findViewById(R.id.welcome_text);
        messageEditText=findViewById(R.id.message_edit_text);
        sendButton=findViewById(R.id.send_btn);

        messageAdapter= new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
llm.setStackFromEnd(true);
recyclerView.setLayoutManager(llm);

sendButton.setOnClickListener((v)->{
    String question = messageEditText.getText().toString().trim();
addToChat(question,Message.SENT_BY_ME);
messageEditText.setText("");
callAPI(question);
welcomeTextView.setVisibility(View.GONE);
});

    }

    void addToChat(String message,String sentBy){
runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      messageList.add(new Message(message, sentBy));
                      messageAdapter.notifyDataSetChanged();
                      recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
                  }

              });


}

void addResponse(String response){
        addToChat(response,Message.SENT_BY_BOT);
}


void callAPI(String question){
    JSONObject jsonBody = new JSONObject();
    try {
        jsonBody.put("model","gpt-3.5-turbo-instruct");//gpt-3.5-turbo-instruct
        jsonBody.put("prompt",question);
        jsonBody.put("max_tokens",4000);
        jsonBody.put("temperature",0);
    } catch (JSONException e) {
        throw new RuntimeException(e);
    }
 RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
 Request request = new Request.Builder()
.url("https://api.openai.com/v1/completions")
.header("Authorization", "Bearer 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")
       .post(body)
        .build();

client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        addResponse("Failed to load response due to"+e.getMessage());

    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
if(response.isSuccessful()){

    JSONObject jsonObject= null;
    try {
        jsonObject = new JSONObject(response.body().string());
        JSONArray jsonArray= jsonObject.getJSONArray("choices");
        String result = jsonArray.getJSONObject(0).getString("text");
        addResponse(result.trim());
    } catch (JSONException e) {
       e.printStackTrace();// throw new RuntimeException(e);
    }



}else {
    addResponse("Failed to load response due to"+response.body().string());
}
    }
});


}}