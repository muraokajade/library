import { Link } from "react-router-dom";
import BookModel from "../../../models/BookModel";

export const SearchBook: React.FC<{ book: BookModel }> = ({ book }) => {


    return (
        <div className='card mt-3 shadow p-3 mb-3 bg-body rounded'>
            <div className='row g-0'>
                <div className='col-md-2'>
                    <div className='d-none d-lg-block'>
                        {book.img ?
                            <img src={book.img}
                                width='123'
                                height='196'
                                alt='Book'
                            />
                            :
                            <img src={require('../../../Images/BooksImages/book-luv2code-1000.png')}
                                width='123'
                                height='196'
                                alt='Book'
                            />
                        }
                    </div>
                    <div className='d-lg-none d-flex justify-content-center 
                        align-items-center'>
                        {book.img ?
                            <img src={book.img}
                                width='123'
                                height='196'
                                alt='Book'
                            />
                            :
                            <img src={require('../../../Images/BooksImages/book-luv2code-1000.png')}
                                width='123'
                                height='196'
                                alt='Book'
                            />
                        }
                    </div>
                </div>
                <div className='col-md-6'>
                    <div className='card-body'>
                        <h5 className='card-title'>
                            {book.author}
                        </h5>
                        <h4>
                            {book.title}
                        </h4>
                        <p className='card-text'>
                            {book.description}
                        </p>
                    </div>
                </div>
                <div className='col-md-4 d-flex justify-content-center align-items-center'>
                    <Link className='btn btn-md main-color text-white' to={`/checkout/${book.id}`}>
                        詳細を見る
                    </Link>
                </div>
            </div>
        </div>
    );
};
