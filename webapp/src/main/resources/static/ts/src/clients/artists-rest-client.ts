import axios, { AxiosError, AxiosResponse } from "axios";
import { axiosConfig } from "../config/axios.config";
import { MyArtistsResponse } from "../model/my-artists-response.model";

export class ArtistsRestClient {

    private readonly MY_ARTISTS_URL = "/rest/v1/my-artists";
    private readonly FOLLOW_ARTISTS_URL = "/rest/v1/artists/follow";
    private readonly UNFOLLOW_ARTISTS_URL = "/rest/v1/artists/unfollow";

    constructor() {
    }

    public async fetchMyArtists(): Promise<MyArtistsResponse> {
        axiosConfig.params = {
            page: this.getPageFromUrl()
        }
        return axios.get(
            this.MY_ARTISTS_URL, axiosConfig
        ).then((response: AxiosResponse<MyArtistsResponse>) => {
            return response.data;
        }).catch((error: AxiosError) => {
            console.error(error);
            throw error;
        });
    }

    public followArtist(artistId: string, source: string): void {
        axios.post(
            `${this.FOLLOW_ARTISTS_URL}/${source}/${artistId}`,
            {},
            axiosConfig
        ).catch((error: AxiosError) => {
            throw error;
        });
    }

    public unfollowArtist(artistId: string, source: string): void {
        axios.post(
            `${this.UNFOLLOW_ARTISTS_URL}/${source}/${artistId}`,
            {},
            axiosConfig
        ).catch((error: AxiosError) => {
            throw error;
        });
    }

    private getPageFromUrl(): string {
        const url = new URL(window.location.href);
        let page = url.searchParams.get("page") || "1";
        if (Number.isNaN(page) || +page < 1) {
            page = "1";
        }

        return page;
    }
}
