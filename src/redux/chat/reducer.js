import { ActionTypes, initialState } from './store';
import { formatGiftedChatMessage } from '../../utils/chat';

function chatReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.AUTHENTICATED: {
      return {
        ...state,
        authToken: action.authToken,
        userId: action.userId
      };
    }
    case ActionTypes.BOT_INIT_STARTED: {
      return {
        ...state,
        latestInit: new Date().toISOString()
      };
    }
    case ActionTypes.MESSAGE_SENT: {
      return {
        ...state,
        messages: [action.chatMessage, ...state.messages]
      };
    }
    case ActionTypes.BOT_RESPONSE_RECEIVED: {
      const chatMessage = formatGiftedChatMessage(action.response);

      return chatMessage
        ? { ...state, messages: [chatMessage, ...state.messages] }
        : state;
    }
    default:
      return state;
  }
}

export default chatReducer;
