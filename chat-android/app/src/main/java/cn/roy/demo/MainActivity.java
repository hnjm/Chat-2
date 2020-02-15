package cn.roy.demo;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.concurrent.Callable;

import cn.roy.demo.adapter.AdapterViewHolder;
import cn.roy.demo.adapter.CommonAdapter;
import cn.roy.demo.chat.ChatClient;
import cn.roy.demo.chat.MessageManager;
import cn.roy.demo.chat.message.ChatMessage;
import cn.roy.demo.chat.util.LogUtil;
import cn.roy.demo.model.User;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    private final static String TAG = "MainActivity";
    private final CompositeDisposable disposables = new CompositeDisposable();

    private CommonAdapter<User> adapter;

    private TextView tv_left, tv_title, tv_right;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_left = findViewById(R.id.tv_left);
        tv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRunSchedulerExampleButtonClicked();
            }
        });
        tv_left.setVisibility(View.GONE);

        tv_title = findViewById(R.id.tv_title);
        tv_right = findViewById(R.id.tv_right);
        tv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatClient chatClient = ChatClient.getInstance();
                if (chatClient.isConnectSuccess()) {
                    chatClient.disConnectServer();
                } else {
                    chatClient.connectServer();
                }
            }
        });

        adapter = new CommonAdapter<User>(this, R.layout.item_user_list,
                MessageManager.getInstance().getUserList()) {
            @Override
            public void convert(AdapterViewHolder holder, User user) {
                TextView tv = holder.getView(R.id.tv_user);
                tv.setText(user.getName());
            }
        };
        lv = findViewById(R.id.lv);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = MessageManager.getInstance().getUserList().get(position);
                Bundle bundle = new Bundle();
                bundle.putString("userId", user.getId());
                bundle.putString("userName", user.getName());
                jump(ChatActivity.class, false, bundle);
            }
        });

        // 定义观察者
        ChatClient.getInstance().getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 1) {
                            tv_title.setText("已连接");
                            tv_right.setText("断开");
                        } else {
                            tv_title.setText("未连接");
                            tv_right.setText("连接");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        MessageManager.getInstance().getUserListObservable()
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer value) {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        Observer<ChatMessage> observer = new Observer<ChatMessage>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ChatMessage chatMessage) {
                LogUtil.log("MainActivity收到通知");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

        MessageManager.getInstance().getMessageListObservable().subscribe(observer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disposables.clear();
    }

    /**********功能：RxJava测试**********/
    void onRunSchedulerExampleButtonClicked() {
        disposables.add(sampleObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete()");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override
                    public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ")");
                    }
                }));
    }

    static Observable<String> sampleObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends String>>() {
            @Override
            public ObservableSource<? extends String> call() throws Exception {
                // Do some long running operation
                SystemClock.sleep(5000);
                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }

}