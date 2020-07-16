import { ActionTypes, initialState } from './store';
import { act } from 'react-test-renderer';

function chatReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.AUTHENTICATED: {
      return {
        ...state,
        authToken: action.authToken,
        userId: action.userId
      };
    }
    case ActionTypes.BOT_RESPONSE_RECEIVED: {
      console.log(action.response);
      return state;
    }
    default:
      return state;
  }
}

export default chatReducer;
