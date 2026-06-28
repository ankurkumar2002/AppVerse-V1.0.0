import { Timestamp } from "rxjs";

export interface CartItemResponse {
    applicationId : string,
    applicationName : string,
    quantity : number,
    unitPrice : number,
    currency : string,
    isFree : boolean,
    thumbnailUrl : string,
    addedAt : string

}