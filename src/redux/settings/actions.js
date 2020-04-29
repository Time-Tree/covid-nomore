import { ActionTypes } from './store';

class SettingsActions {
  changeStatusAction(status) {
    return dispatch => {
      dispatch({
        type: ActionTypes.CHANGE_STATUS,
        payload: status
      });
    };
  }
}

const settingsActions = new SettingsActions();
export default settingsActions;
