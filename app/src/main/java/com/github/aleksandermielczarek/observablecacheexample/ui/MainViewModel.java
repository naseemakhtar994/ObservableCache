package com.github.aleksandermielczarek.observablecacheexample.ui;

import android.databinding.ObservableField;
import android.support.annotation.Nullable;

import com.github.aleksandermielczarek.observablecache.ObservableCache;
import com.github.aleksandermielczarek.observablecacheexample.service.CachedService;
import com.github.aleksandermielczarek.observablecacheexample.service.ObservableService;

import org.parceler.Parcel;

import javax.inject.Inject;

import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Aleksander Mielczarek on 30.10.2016.
 */

public class MainViewModel {

    public static final String OBSERVABLE_CACHE_KEY_OBSERVABLE = "observable";
    public static final String OBSERVABLE_CACHE_KEY_SINGLE = "single";
    public static final String OBSERVABLE_CACHE_KEY_COMPLETABLE = "completable";
    public static final String OBSERVABLE_CACHE_KEY_OBSERVABLE_ERROR = "observableError";
    public static final String OBSERVABLE_CACHE_KEY_SINGLE_ERROR = "singleError";
    public static final String OBSERVABLE_CACHE_KEY_COMPLETABLE_ERROR = "completableError";

    public final ObservableField<String> result = new ObservableField<>();

    private final ObservableService observableService;
    private final ObservableCache observableCache;
    private final CachedService cachedService;
    private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    public MainViewModel(ObservableService observableService, ObservableCache observableCache, CachedService cachedService) {
        this.observableService = observableService;
        this.observableCache = observableCache;
        this.cachedService = cachedService;
    }

    public void testObservable() {
        testObservable(observableService.observable()
                .compose(observableCache.cacheObservable(OBSERVABLE_CACHE_KEY_OBSERVABLE)));
    }

    public void testSingle() {
        testSingle(observableService.single()
                .compose(observableCache.cacheSingle(OBSERVABLE_CACHE_KEY_SINGLE)));
    }

    public void testCompletable() {
        testCompletable(observableService.completable()
                .compose(observableCache.cacheCompletable(OBSERVABLE_CACHE_KEY_COMPLETABLE)));
    }

    public void testObservableError() {
        testObservable(observableService.observableError()
                .compose(observableCache.cacheObservable(OBSERVABLE_CACHE_KEY_OBSERVABLE_ERROR)));
    }

    public void testSingleError() {
        testSingle(observableService.singleError()
                .compose(observableCache.cacheSingle(OBSERVABLE_CACHE_KEY_SINGLE_ERROR)));
    }

    public void testCompletableError() {
        testCompletable(observableService.completableError()
                .compose(observableCache.cacheCompletable(OBSERVABLE_CACHE_KEY_COMPLETABLE_ERROR)));
    }

    public void testServiceObservable() {
        testObservable(observableService.observable()
                .compose(cachedService.observable()));
    }

    public void testServiceSingle() {
        testSingle(observableService.single()
                .compose(cachedService.single()));
    }

    public void testServiceCompletable() {
        testCompletable(observableService.completable()
                .compose(cachedService.completable()));
    }

    public void testServiceObservableError() {
        testObservable(observableService.observableError()
                .compose(cachedService.observableError()));
    }

    public void testServiceSingleError() {
        testSingle(observableService.singleError()
                .compose(cachedService.singleError()));
    }

    public void testServiceCompletableError() {
        testCompletable(observableService.completableError()
                .compose(cachedService.completableError()));
    }

    private void testObservable(Observable<String> testObservable) {
        subscriptions.add(testObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result::set, throwable -> result.set(throwable.getMessage())));
    }

    public void testSingle(Single<String> testSingle) {
        subscriptions.add(testSingle
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result::set, throwable -> result.set(throwable.getMessage())));
    }

    public void testCompletable(Completable completable) {
        subscriptions.add(completable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> result.set("completable"), throwable -> result.set(throwable.getMessage())));
    }

    public void restoreObservables() {
        observableCache.<String>getObservable(OBSERVABLE_CACHE_KEY_OBSERVABLE).ifPresent(this::testObservable);
        observableCache.<String>getSingle(OBSERVABLE_CACHE_KEY_SINGLE).ifPresent(this::testSingle);
        observableCache.getCompletable(OBSERVABLE_CACHE_KEY_COMPLETABLE).ifPresent(this::testCompletable);
        observableCache.<String>getObservable(OBSERVABLE_CACHE_KEY_OBSERVABLE_ERROR).ifPresent(this::testObservable);
        observableCache.<String>getSingle(OBSERVABLE_CACHE_KEY_SINGLE_ERROR).ifPresent(this::testSingle);
        observableCache.getCompletable(OBSERVABLE_CACHE_KEY_COMPLETABLE_ERROR).ifPresent(this::testCompletable);

        cachedService.cachedObservable().ifPresent(this::testObservable);
        cachedService.cachedSingle().ifPresent(this::testSingle);
        cachedService.cachedCompletable().ifPresent(this::testCompletable);
        cachedService.cachedObservableError().ifPresent(this::testObservable);
        cachedService.cachedSingleError().ifPresent(this::testSingle);
        cachedService.cachedCompletableError().ifPresent(this::testCompletable);
    }

    public void clear() {
        result.set("");
        unsubscribe();
        observableCache.clear();
    }

    public void unsubscribe() {
        subscriptions.clear();
    }

    public State saveState() {
        return new State(result.get());
    }

    public void restoreState(@Nullable State state) {
        if (state != null) {
            result.set(state.getResult());
        }
    }

    @Parcel
    public static class State {

        String result;

        public State() {

        }

        public State(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

}
