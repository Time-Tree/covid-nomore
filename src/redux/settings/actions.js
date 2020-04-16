import { ActionTypes } from './store';

class SettingsActions {
  changePublishCodeAction(publishCode) {
    return dispatch => {
      dispatch({
        type: ActionTypes.CHANGE_PUBLISH_CODE,
        payload: publishCode
      });
    };
  }
}

const settingsActions = new SettingsActions();
export default settingsActions;
