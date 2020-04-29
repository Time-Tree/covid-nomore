import { ActionTypes, initialState } from './store';

function settingsReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.CHANGE_STATUS: {
      return {
        ...state,
        isSubscribing: action.payload.isSubscribing,
        isConnected: action.payload.isConnected
      };
    }
    default:
      return state;
  }
}

export default settingsReducer;
