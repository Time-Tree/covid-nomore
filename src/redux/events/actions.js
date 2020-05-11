import { ActionTypes } from './store';

class EventsActions {
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

  setSync(payload) {
    return dispatch => {
      dispatch({
        type: ActionTypes.SET_SYNC,
        payload
      });
    };
  }
}

const eventActions = new EventsActions();
export default eventActions;
