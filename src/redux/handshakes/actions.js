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
}

const hadshakesActions = new HandshakeActions();
export default hadshakesActions;
