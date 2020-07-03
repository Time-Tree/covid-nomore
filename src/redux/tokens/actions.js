import axios from 'axios';
import { ActionTypes } from './store';
import { getTokens, deleteTokens } from '../../utils/tokens';

const API_URL = 'http://localhost:3100/';

class TokensActions {
  getTokensAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.GET_TOKENS
      });
      try {
        const response = await getTokens();
        dispatch({
          type: ActionTypes.GET_TOKENS_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.GET_TOKENS_FAILED,
          payload: error
        });
      }
    };
  }

  deleteTokensAction() {
    return async dispatch => {
      dispatch({
        type: ActionTypes.DELETE_TOKENS
      });
      try {
        const response = await deleteTokens();
        dispatch({
          type: ActionTypes.DELETE_TOKENS_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.DELETE_TOKENS_FAILED,
          payload: error
        });
      }
    };
  }

  sendTokensAction(tokens) {
    return async dispatch => {
      dispatch({
        type: ActionTypes.SEND_TOKENS
      });
      try {
        const response = await axios.post(`${API_URL}infected`, tokens);
        dispatch({
          type: ActionTypes.SEND_TOKENS_SUCCESS,
          payload: response
        });
      } catch (error) {
        dispatch({
          type: ActionTypes.SEND_TOKENS_FAILED,
          payload: error
        });
      }
    };
  }

  addTokenAction(token) {
    return dispatch => {
      dispatch({
        type: ActionTypes.ADD_TOKEN,
        payload: token
      });
    };
  }

  clearTokensAction() {
    return dispatch => {
      dispatch({
        type: ActionTypes.CLEAR_TOKENS
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

const tokenActions = new TokensActions();
export default tokenActions;
