export class DateService {

    constructor() {
    }

    public calculatePastTimeRange(date: Date) {
        const now = Date.now();
        const then = new Date(date).valueOf();
        return Math.round((now - then) / 1000 / 60 / 60 / 24);
    }

    public calculateFutureTimeRange(date: Date) {
        const now = Date.now();
        const then = new Date(date).valueOf();
        return Math.round((then - now) / 1000 / 60 / 60 / 24);
    }
}
