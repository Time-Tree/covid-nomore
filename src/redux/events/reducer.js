import { ActionTypes, initialState } from './store';

function settingsReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.ADD_EVENT: {
      const newEvents = [...action.payload, ...state.events].slice(0, 100);
      return {
        ...state,
        events: newEvents
      };
    }
    case ActionTypes.CLEAR_EVENTS: {
      return {
        ...state,
        events: []
      };
    }
    case ActionTypes.SET_SYNC: {
      return {
        ...state,
        sync: action.payload
      };
    }
    default:
      return state;
  }
}

export default settingsReducer;
