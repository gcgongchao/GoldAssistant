package com.parttime.addresslist;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.parttime.IM.activitysetting.GroupSettingActivity;
import com.parttime.base.WithTitleActivity;
import com.parttime.constants.ActivityExtraAndKeys;
import com.qingmu.jianzhidaren.R;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UserDetailActivity extends WithTitleActivity implements View.OnClickListener {

    private String groupId;
    private String userId;
    private boolean isGroupOwner = false;
    //0:没有获取成功 1:禁言 2:非禁言
    private int forbiddenValue = 0;

    private ViewPager viewPager ;

    private UserDetailPagerAdapter adapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_user_detail);
        super.onCreate(savedInstanceState);

        initView();

        bindData();
    }

    private void initView() {
        left(TextView.class, R.string.back);
        right(TextView.class, R.string.more ,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMore();
            }
        });
        center(R.string.user_detail);

        viewPager = (ViewPager)findViewById(R.id.viewPager);

        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add("1");
        set.add("2");
        set.add("3");
        set.add("4");
        set.add("5");
        set.add("6");
        set.add("7");
        set.add("8");
        adapter = new UserDetailPagerAdapter(getSupportFragmentManager());
        adapter.setData(set);
        viewPager.setAdapter(adapter);
    }

    private void bindData() {
        groupId = getIntent().getStringExtra(ActivityExtraAndKeys.GroupSetting.GROUPID);
        userId = getIntent().getStringExtra(ActivityExtraAndKeys.USER_ID);
        isGroupOwner = getIntent().getBooleanExtra(ActivityExtraAndKeys.GroupSetting.GROUPOWNER,false);

        if(isGroupOwner){
            rightWrapper.setVisibility(View.VISIBLE);
        }else{
            rightWrapper.setVisibility(View.GONE);
        }

        new Thread(new Runnable() {

            public void run() {
                try {
                    List<String> blockedList = EMGroupManager.getInstance()
                            .getBlockedUsers(groupId);
                    if (blockedList != null) {
                        if(blockedList.contains(userId)){
                            forbiddenValue = 1;
                        }else{
                            forbiddenValue = 2;
                        }
                    }
                } catch (EaseMobException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "获取失败,请检查网络或稍后再试", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected ViewGroup getLeftWrapper() {return null;}

    @Override
    protected ViewGroup getRightWrapper() {return null;}

    @Override
    protected TextView getCenter() {return null;}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }

    private Dialog showMore() {
        final Dialog more = new Dialog(this, R.style.ActionSheet);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.activity_person_detail_more, null);
        final int cFullFillWidth = 10000;
        layout.setMinimumWidth(cFullFillWidth);

        //禁言
        final TextView forbiddenTable = (TextView) layout.findViewById(R.id.forbidden_talk);
        //取消禁言
        TextView cancelForbiddenTalk = (TextView) layout.findViewById(R.id.cancel_forbidden_talk);
        //更新群备注
        TextView updateGroupNotice = (TextView) layout.findViewById(R.id.update_group_notice);
        //取消
        TextView cancel = (TextView) layout.findViewById(R.id.cancel);

        forbiddenTable.setVisibility(View.GONE);
        cancelForbiddenTalk.setVisibility(View.GONE);
       if(forbiddenValue == 1){
           cancelForbiddenTalk.setVisibility(View.VISIBLE);
        }else if(forbiddenValue == 2){
           forbiddenTable.setVisibility(View.VISIBLE);
        }

        // 禁言
        forbiddenTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                more.dismiss();
                showWait(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EMGroupManager.getInstance().blockUser(groupId,
                                    userId);
                            forbiddenValue = 1;
                        } catch (EaseMobException e) {
                            e.printStackTrace();
                        }
                        showWait(false);
                    }
                }).start();


            }
        });

        //取消禁言
        cancelForbiddenTalk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                more.dismiss();
                showWait(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EMGroupManager.getInstance().unblockUser(groupId,
                                    userId);
                            forbiddenValue = 2;
                        } catch (EaseMobException e) {
                            e.printStackTrace();
                        }
                        showWait(false);
                    }
                }).start();

            }
        });

        //更新群备注
        updateGroupNotice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailActivity.this, GroupSettingActivity.class);
                intent.putExtra(ActivityExtraAndKeys.GroupSetting.GROUPID, groupId);
                startActivity(intent);
                more.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                more.dismiss();
            }
        });

        Window w = more.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = -1000;
        lp.gravity = Gravity.BOTTOM;
        more.onWindowAttributesChanged(lp);
        more.setCanceledOnTouchOutside(false);
        more.setContentView(layout);
        more.show();

        return more;
    }
}
