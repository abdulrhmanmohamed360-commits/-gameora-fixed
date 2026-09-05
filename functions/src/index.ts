import { initializeApp } from "firebase-admin/app";

initializeApp();

export {
  requestWalletDeposit,
  requestWalletWithdrawal,
} from "./wallet";

export {
  createOrder,
  completeOrder,
  disputeOrder,
} from "./orders";

export {
  createProduct,
  updateProduct,
  deactivateProduct,
} from "./products";

export {
  acceptOrder,
  rejectOrder,
  sendChatMessage,
  markMessageAsRead,
} from "./chat";
