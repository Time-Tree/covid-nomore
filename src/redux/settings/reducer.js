import { ActionTypes, initialState } from './store';

function settingsReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.CHANGE_STATUS: {
      return {
        ...state,
        status: action.payload
      };
    }
    case ActionTypes.SET_CLIENT_ID: {
      return {
        ...state,
        clientId: action.payload
      };
    }
    case ActionTypes.SET_EASTER_EGG: {
      return {
        ...state,
        easterEgg: action.payload
      };
    }
    case ActionTypes.SET_PROCESS: {
      return {
        ...state,
        [action.payload.type]: action.payload.value
      };
    }
    case ActionTypes.SET_NEARBY_PROCESS: {
      return {
        ...state,
        nearbyProcess: action.payload
      };
    }
    case ActionTypes.SET_BLE_PROCESS: {
      return {
        ...state,
        bleProcess: action.payload
      };
    }
    case ActionTypes.SET_NEARBY_STATUS: {
      return {
        ...state,
        nearbyStatus: action.payload
      };
    }
    case ActionTypes.SET_BLE_STATUS: {
      return {
        ...state,
        bleStatus: action.payload
      };
    }
    default:
      return state;
  }
}

export default settingsReducer;
