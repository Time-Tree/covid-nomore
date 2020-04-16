import { ActionTypes } from './store';

class SettingsActions {
  addEventAction(event) {
    return dispatch => {
      dispatch({
        type: ActionTypes.ADD_EVENT,
        payload: event
      });
    };
  }

  clearEventsAction() {
    return dispatch => {
      dispatch({
        type: ActionTypes.CLEAR_EVENTS
      });
    };
  }
}

const settingsActions = new SettingsActions();
export default settingsActions;
