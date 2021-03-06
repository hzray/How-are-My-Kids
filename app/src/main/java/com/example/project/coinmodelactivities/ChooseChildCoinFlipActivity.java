package com.example.project.coinmodelactivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.example.project.childactivities.EditChildActivity;
import com.example.project.childmodel.Child;
import com.example.project.childmodel.ChildManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.project.coinflipmodel.CoinFlipQueue;
import com.example.project.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Allows the user to choose which child that will get the coinflip.
 * Prompts child with choice.
 * <p>
 * Passes the index of the child and the choice of heads or tails.
 */
public class ChooseChildCoinFlipActivity extends AppCompatActivity {

    private static final String UP_TAG = "UP";
    private static final String CHILD_MANAGER_TAG = "ChildManager";
    private static final String QUEUE_TAG = "Queue";
    private static final String EXTRA_INDEX = "CoinFlip - ChildIndex";
    private static final String APP_PREFS_NAME = "AppPrefs";
    private static final String QUEUE_PREFS_NAME = "QueuePrefs";
    private ChildManager childManager;
    private CoinFlipQueue coinFlipQueue;
    private int childIndex;

    public static Intent makeLaunchIntent(Context context, int index) {
        Intent intent = new Intent(context, ChooseChildCoinFlipActivity.class);
        intent.putExtra(EXTRA_INDEX, index);
        return intent;
    }

    // -1 Index means top of the list, -2 means no child.
    private void extractDataFromIntent() {
        Intent intent = getIntent();
        childIndex = intent.getIntExtra(EXTRA_INDEX, -1);

        if (childIndex == -1 &&
                childManager.getLength() != 0) {
            childIndex = childManager.findChildIndexById(coinFlipQueue.get(0));
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_child_coin_flip);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Loads Children History
        childManager = ChildManager.getInstance();
        coinFlipQueue = CoinFlipQueue.getInstance();

        loadChildData();
        checkIfAnyChildrenInManager();

        setupQueue();
        extractDataFromIntent();

        // Enable "up" on toolbar
        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.println(Log.ERROR, UP_TAG, "Up bar Error:" + e.getMessage());
        }

        if (childIndex == -2) {
            launchCoinFlipActivity(false, -1);
        }

        if (childManager.getLength() > 0 && childIndex != -2) {
            populateFields();
        }

        Log.println(Log.INFO, CHILD_MANAGER_TAG, childManager.getLength() + "");
    }

    private void setupQueue() {
        List<Integer> childIdList = getListOfChildrenId();
        coinFlipQueue.setQueue(getCoinQueue(ChooseChildCoinFlipActivity.this));
        coinFlipQueue.removeMissingIds(childIdList);
        coinFlipQueue.addMissingNewIds(childIdList);
    }

    private List<Integer> getListOfChildrenId() {
        List<Integer> childIdList = new ArrayList<>();
        for (Child child : childManager.getChildList()) {
            childIdList.add(child.getId());
        }
        return childIdList;
    }

    private void populateFields() {
        setOracleText();
        setChildNameTag();
        setAvatar();
        setOnClickHeadsTails();
        setOnClickChangeChild();
    }

    private void setOnClickChangeChild() {
        Button changeChild = (Button) findViewById(R.id.coin_flip_choose_change_child);
        changeChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchChangeChildActivity();
            }
        });
    }

    private void setOracleText() {
        TextView oracleText = (TextView) findViewById(R.id.coin_flip_choose_oracle);
        String resourceString = getString(R.string.coin_flip_choose_oracle);
        oracleText.setText(String.format(resourceString, childManager.getChildName(childIndex)));
    }

    private void setChildNameTag() {
        TextView name = (TextView) findViewById(R.id.coin_flip_choose_child_name);
        name.setText(childManager.getChildName(childIndex));
    }

    private void setAvatar() {
        ImageView childAvatar = (ImageView) findViewById(R.id.coin_flip_choose_avatar);
        Uri avatarUri = Uri.parse(childManager.getChildAvatarUriPath(childIndex));
        try {
            childAvatar.setImageURI(avatarUri);
        } catch (RuntimeException e) {
            childAvatar.setImageURI(Child.DEFAULT_URI);
        }
    }

    private void setOnClickHeadsTails() {
        ImageButton heads_button = (ImageButton) findViewById(R.id.coin_flip_choose_heads_image);
        ImageButton tails_button = (ImageButton) findViewById(R.id.coin_flip_choose_tails_image);
        Button heads_text = (Button) findViewById(R.id.coin_flip_choose_heads_text_button);
        Button tails_text = (Button) findViewById(R.id.coin_flip_choose_tails_text_button);
        //set onClickListener
        View.OnClickListener heads_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCoinFlipActivity(true, childIndex);
            }
        };

        View.OnClickListener tails_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCoinFlipActivity(false, childIndex);
            }
        };

        heads_button.setOnClickListener(heads_listener);
        heads_text.setOnClickListener(heads_listener);
        tails_button.setOnClickListener(tails_listener);
        tails_text.setOnClickListener(tails_listener);
    }

    private void launchCoinFlipActivity(boolean choice, int index) {
        Intent intent = CoinFlipActivity.makeLaunchIntent(ChooseChildCoinFlipActivity.this, index, choice);
        startActivity(intent);
        finish();
    }

    private void launchChangeChildActivity() {
        Intent intent = ChangeChildCoinFlipActivity.makeLaunchIntent(ChooseChildCoinFlipActivity.this);
        startActivity(intent);
        finish();
    }

    private void checkIfAnyChildrenInManager() {
        Log.println(Log.INFO, CHILD_MANAGER_TAG, "Number of Children: " + childManager.getLength());
        if (childManager.getLength() == 0) {
            Log.println(Log.INFO, CHILD_MANAGER_TAG, "No Children, moving onto coin flip");
            launchCoinFlipActivity(false, -1);
        }
    }

    private void loadChildData() {
        if (childManager.getLength() == 0) {
            List<Child> savedChildList = EditChildActivity.getSavedChildList(ChooseChildCoinFlipActivity.this);
            if (savedChildList != null) {
                childManager.setChildList(savedChildList);
                Log.println(Log.INFO, CHILD_MANAGER_TAG, "Loaded Child List from EditKidsActivity");
            }
        }
    }

    // Reference: https://www.youtube.com/watch?v=jcliHGR3CHo&ab_channel=CodinginFlow
    public static void saveCoinQueue(Context context, List<Integer> coinQueue) {
        SharedPreferences prefs = context.getSharedPreferences(APP_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(coinQueue);
        editor.putString(QUEUE_PREFS_NAME, json);
        editor.apply();
        Log.println(Log.INFO, QUEUE_TAG, "Saved Child Queue: " + coinQueue);
    }

    public static List<Integer> getCoinQueue(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(APP_PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(QUEUE_PREFS_NAME, null);
        Type type = new TypeToken<ArrayList<Integer>>() {
        }.getType();
        List<Integer> queue = gson.fromJson(json, type);
        Log.println(Log.INFO, QUEUE_TAG, "Loaded Child Queue: " + queue);
        return queue;
    }

}

