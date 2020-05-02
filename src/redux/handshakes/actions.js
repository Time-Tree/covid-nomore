import { ActionTypes } from './store';

class HandshakeActions {
  addHandshakeAction(handshake) {
    return dispatch => {
      dispatch({
        type: ActionTypes.ADD_HANDSHAKE,
        payload: handshake
      });
    };
  }

  changeHandshakeAction(handshake) {
    return dispatch => {
      dispatch({
        type: ActionTypes.CHANGE_HANDHAKE,
        payload: handshake
      });
    };
  }

  clearHandshakeAction() {
    return dispatch => {
      dispatch({
        type: ActionTypes.CLEAR_HANSHAKES
      });
    };
  }

  saveHandshakeAction() {
    return dispatch => {
      dispatch({
        type: ActionTypes.SAVE_HANDSHAKES
      });
    };
  }

  removeHanshakeAction(time) {
    return dispatch => {
      dispatch({
        type: ActionTypes.REMOVE_HANDSHAKE,
        payload: time
      });
    };
  }
}

const hadshakesActions = new HandshakeActions();
export default hadshakesActions;
