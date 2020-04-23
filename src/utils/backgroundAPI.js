import BackgroundFetch from 'react-native-background-fetch';
import NearbyAPI from './nearbyAPI';

class BackgroundAPI {
  eventListener = null;

  init = () => {
    this.configure();
    this.checkStatus();
  };

  configure = () => {
    BackgroundFetch.configure(
      {
        minimumFetchInterval: 15, // <-- minutes (15 is minimum allowed)
        stopOnTerminate: false, // only for Android
        startOnBoot: true, // only for Android,
        enableHeadless: true
      },
      taskId => {
        console.log('[js] Received background-fetch event: ', taskId);
        NearbyAPI.init();
        BackgroundFetch.finish(taskId);
      },
      error => {
        console.log('[js] RNBackgroundFetch failed to start');
      }
    );
    BackgroundFetch.registerHeadlessTask(this.myHeadlessTask);
  };

  checkStatus = () => {
    // Android will always return STATUS_AVAILABLE
    BackgroundFetch.status(status => {
      switch (status) {
        case BackgroundFetch.STATUS_RESTRICTED:
          console.log('BackgroundFetch restricted');
          break;
        case BackgroundFetch.STATUS_DENIED:
          console.log('BackgroundFetch denied');
          break;
        case BackgroundFetch.STATUS_AVAILABLE:
          console.log('BackgroundFetch is enabled');
          break;
      }
    });
  };

  myHeadlessTask = event => {
    let taskId = event.taskId;
    console.log('[BackgroundFetch HeadlessTask] start: ', taskId);
    NearbyAPI.init();
    BackgroundFetch.finish(taskId);
  };
}

export default new BackgroundAPI();
