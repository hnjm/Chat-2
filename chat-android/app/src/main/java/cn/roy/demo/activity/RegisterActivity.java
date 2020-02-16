package cn.roy.demo.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.roy.demo.R;
import cn.roy.demo.activity.BaseActivity;
import cn.roy.demo.activity.MainActivity;
import cn.roy.demo.chat.util.LogUtil;
import cn.roy.demo.model.User;
import cn.roy.demo.util.CacheManager;
import cn.roy.demo.util.SPUtil;
import cn.roy.demo.util.http.HttpResponseException;
import cn.roy.demo.util.http.HttpUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @Description:
 * @Author: Roy
 * @Date: 2019/1/30 15:47
 * @Version: v1.0
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private EditText et_user_name, et_user_password;
    private TextView tv_login, tv_register, tv_forget_password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        et_user_name = findViewById(R.id.et_user_name);
//        et_user_password = findViewById(R.id.et_user_password);
//        tv_login = findViewById(R.id.tv_login);
//        tv_register = findViewById(R.id.tv_register);
//        tv_forget_password = findViewById(R.id.tv_forget_password);
//
//        tv_login.setOnClickListener(this);
//        tv_register.setOnClickListener(this);
//        tv_forget_password.setOnClickListener(this);
//
//        et_user_name.setText(SPUtil.getString(SPUtil.LOGIN_NAME, ""));
//        et_user_password.setText(SPUtil.getString(SPUtil.LOGIN_PASSWORD, ""));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_login:
                String userName = et_user_name.getText().toString().trim();
                if (TextUtils.isEmpty(userName)) {
                    toast("用户名不能为空");
                    return;
                }
                String userPassword = et_user_password.getText().toString().trim();
                if (TextUtils.isEmpty(userPassword)) {
                    toast("密码不能为空");
                    return;
                }

                User user = new User();
                user.setName(userName);
                user.setPassword(userPassword);

                if (progressDialog == null) {
                    initProgressDialog();
                }
                progressDialog.show();
                HttpUtil.getInstance().post("user/login", user,
                        new Observer<JSONObject>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(JSONObject jsonObject) {
                                progressDialog.dismiss();
                                User u = JSON.parseObject(jsonObject.toString(), User.class);
                                CacheManager.getInstance().cacheCurrentUser(u);
                                SPUtil.saveParam(SPUtil.LOGIN_NAME,
                                        et_user_name.getText().toString().trim());
                                SPUtil.saveParam(SPUtil.LOGIN_PASSWORD,
                                        et_user_password.getText().toString().trim());
                                SPUtil.saveParam(SPUtil.USER_INFO, jsonObject.toString());
                                jump(MainActivity.class, true, null);
                            }

                            @Override
                            public void onError(Throwable e) {
                                progressDialog.dismiss();
                                if (e instanceof HttpResponseException) {
                                    LogUtil.log("错误码：" + ((HttpResponseException) e).getCode());
                                }
                                toast(e.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                LogUtil.log("结束");
                            }
                        });
                break;
            case R.id.tv_register:
                toast("注册");
                break;
            case R.id.tv_forget_password:
                toast("忘记密码");
                break;
            default:
                break;
        }
    }

    private ProgressDialog progressDialog;

    private void initProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("提示");
            progressDialog.setMessage("登录中...");
        }
    }

}
