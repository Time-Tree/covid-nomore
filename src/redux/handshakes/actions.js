import axios from 'axios';
import { ActionTypes } from './store';
import { getHandshakes, deleteHandshakes } from '../../utils/tokens';

const API_URL = 'http://localhost:3100/';

class HandshakeActions {
  getHandshakesAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.GET_HANDSHAKES
      });
      try {
        const response = await getHandshakes();
        dispatch({
          type: ActionTypes.GET_HANDSHAKES_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.GET_HANDSHAKES_FAILED,
          payload: error
        });
      }
    };
  }

  deleteHandshakesAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.DELETE_HANDSHAKES
      });
      try {
        const response = await deleteHandshakes();
        dispatch({
          type: ActionTypes.DELETE_HANDSHAKES_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.DELETE_HANDSHAKES_FAILED,
          payload: error
        });
      }
    };
  }

  sendHandshakesAction(handshakes) {
    return async dispatch => {
      dispatch({
        type: ActionTypes.SEND_HANDSHAKES
      });
      try {
        const response = await axios.post(`${API_URL}infected`, handshakes);
        dispatch({
          type: ActionTypes.SEND_HANDSHAKES_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.SEND_HANDSHAKES_FAILED,
          payload: error
        });
      }
    };
  }

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
