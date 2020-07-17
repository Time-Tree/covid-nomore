import { ActionTypes, initialState } from './store';

function chatReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.AUTHENTICATED: {
      return {
        ...state,
        authToken: action.authToken,
        userId: action.userId
      };
    }
    case ActionTypes.MESSAGE_SENT: {
      return {
        ...state,
        messages: [action.chatMessage, ...state.messages]
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
